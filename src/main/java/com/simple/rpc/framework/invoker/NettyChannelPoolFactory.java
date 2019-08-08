package com.simple.rpc.framework.invoker;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.simple.rpc.framework.serialize.handler.NettyDecoderHandler;
import com.simple.rpc.framework.serialize.handler.NettyEncoderHandler;
import com.simple.rpc.framework.serialize.message.ResponseMessage;
import com.simple.rpc.framework.utils.PropertyConfigHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * 客户端ChannelPool工厂
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class NettyChannelPoolFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyChannelPoolFactory.class);

    /**
     * 饿汉单例模式
     */
    private static final NettyChannelPoolFactory INSTANCE = new NettyChannelPoolFactory();
    /**
     * 缓存ChannelPool的Map:Key是服务地址,value是存放为这个地址创建的Channel的阻塞队列
     */
    private static final Map<InetSocketAddress, ArrayBlockingQueue<Channel>> CHANNEL_POOL_MAP = Maps.newConcurrentMap();
    /**
     * 每个服务地址ChannelPool中Channel的数量,在simple-rpc.properties中设置
     */
    private static final int CHANNEL_POOL_SIZE = PropertyConfigHelper.getChannelPoolSize();

    private NettyChannelPoolFactory() {
    }

    public static NettyChannelPoolFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 为服务地址创建ChannelPool并缓存到Map中
     */
    public void registerChannelQueueToMap(InetSocketAddress socketAddress) {
        long startTime = System.currentTimeMillis();
        // 计数器
        int realChannelConnectSize = 0;
        while (realChannelConnectSize < CHANNEL_POOL_SIZE) {
            Channel channel = null;
            while (null == channel) {
                // 若channel创建失败,则重新创建
                channel = registerChannel(socketAddress);
            }
            realChannelConnectSize++;
            // 将创建的Channel存入阻塞队列,并将阻塞队列缓存到Map中
            ArrayBlockingQueue<Channel> channelArrayBlockingQueue = CHANNEL_POOL_MAP.get(socketAddress);
            if (null == channelArrayBlockingQueue) {
                channelArrayBlockingQueue = new ArrayBlockingQueue<Channel>(CHANNEL_POOL_SIZE);
                CHANNEL_POOL_MAP.put(socketAddress, channelArrayBlockingQueue);
            }
            channelArrayBlockingQueue.offer(channel);
        }
        long times = System.currentTimeMillis() - startTime;
        LOGGER.info("创建ChannelPool耗时{}ms:[{}:{}]", times, socketAddress.getHostName(), socketAddress.getPort());
    }

    /**
     * 基于Netty为服务地址创建Channel
     */
    public Channel registerChannel(InetSocketAddress socketAddress) {
        try {
            EventLoopGroup group = new NioEventLoopGroup(10);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.remoteAddress(socketAddress);
            String serializer = PropertyConfigHelper.getClientSerializer();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // 注册消息编码器
                            ch.pipeline().addLast(new NettyEncoderHandler(serializer));
                            // 注册结果解码器
                            ch.pipeline().addLast(new NettyDecoderHandler(ResponseMessage.class));
                            // 注册客户端业务逻辑处理handler
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            final Channel newChannel = channelFuture.channel();
            final CountDownLatch connectedLatch = new CountDownLatch(1);
            final List<Boolean> isSuccessHolder = Lists.newArrayListWithCapacity(1);
            // 监听Channel是否建立成功
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        isSuccessHolder.add(Boolean.TRUE);
                    } else {
                        // 若Channel建立失败,保存建立失败的标记
                        future.cause().printStackTrace();
                        isSuccessHolder.add(Boolean.FALSE);
                    }
                    connectedLatch.countDown();
                }
            });
            // 阻塞等待Channel创建结果
            connectedLatch.await();
            if (isSuccessHolder.get(0)) {
                return newChannel;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 根据地址获取ChannelPool的缓存队列
     */
    public ArrayBlockingQueue<Channel> acquire(InetSocketAddress socketAddress) {
        ArrayBlockingQueue<Channel> arrayBlockingQueue = CHANNEL_POOL_MAP.get(socketAddress);
        // 初始化只是保证初始的所有缓存地址都有了channel通道, 但是服务地址是可变的, 所以新取channel时可能是取不到的, 需要在池中新增channel
        if (null == arrayBlockingQueue) {
            registerChannelQueueToMap(socketAddress);
            return CHANNEL_POOL_MAP.get(socketAddress);
        } else {
            return arrayBlockingQueue;
        }
    }

    /**
     * Channel使用完毕之后,回收到阻塞队列arrayBlockingQueue
     */
    public void release(ArrayBlockingQueue<Channel> arrayBlockingQueue, Channel channel, InetSocketAddress inetSocketAddress) {
        if (null == arrayBlockingQueue) {
            return;
        }
        // 回收之前先检查channel是否可用,不可用的话,重新注册一个,放入阻塞队列
        if (null == channel || !channel.isActive() || !channel.isOpen() || !channel.isWritable()) {
            if (channel != null) {
                channel.deregister().syncUninterruptibly().awaitUninterruptibly();
                channel.closeFuture().syncUninterruptibly().awaitUninterruptibly();
            }
            Channel newChannel = null;
            while (null == newChannel) {
                // 重新创建Channel
                newChannel = registerChannel(inetSocketAddress);
            }
            arrayBlockingQueue.offer(newChannel);
            return;
        }
        arrayBlockingQueue.offer(channel);
    }

}
