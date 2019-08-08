package com.simple.rpc.framework.invoker;

import com.simple.rpc.framework.serialize.message.RequestMessage;
import com.simple.rpc.framework.serialize.message.ResponseMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 客户端发起请求连接时的实际执行逻辑
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class RevokerServiceCallable implements Callable<ResponseMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokerServiceCallable.class);

    /**
     * 服务地址
     */
    private InetSocketAddress inetSocketAddress;
    /**
     * 请求消息
     */
    private RequestMessage request;
    /**
     * 连接服务地址的Channel
     */
    private Channel channel;

    public RevokerServiceCallable(InetSocketAddress inetSocketAddress, RequestMessage request) {
        this.inetSocketAddress = inetSocketAddress;
        this.request = request;
    }

    @Override
    public ResponseMessage call() throws InterruptedException {
        // 创建返回结果包装类,并存入返回结果容器
        ResponseReceiverHolder.initResponseData(request.getTraceId());
        // 根据本地调用服务提供者地址获取对应的Netty通道channel队列
        ArrayBlockingQueue<Channel> blockingQueue = NettyChannelPoolFactory.getInstance().acquire(inetSocketAddress);
        try {
            if (null == channel) {
                // 先尝试从ChannelPool阻塞队列中获取一个可用的Channel
                channel = blockingQueue.poll(100, TimeUnit.MILLISECONDS);
            }
            // 取出来无效或取不到则重新创建一个(需要while循环,因为创建Channel可能失败)
            while (null == channel || !channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
                channel = NettyChannelPoolFactory.getInstance().registerChannel(inetSocketAddress);
            }
            // 将本次调用的信息写入Netty通道,发起异步调用
            LOGGER.info("客户端发送请求消息:[content:{} id:{}]", request, request.getTraceId());
            ChannelFuture channelFuture = channel.writeAndFlush(request);
            channelFuture.syncUninterruptibly();
            // 从结果包装类容器中取结果,会同步阻塞
            return ResponseReceiverHolder.getValue(request.getTraceId(), request.getTimeout());
        } catch (InterruptedException e) {
            LOGGER.error("请求超时,线程已中断!", e);
            throw new InterruptedException();
        } finally {
            // 本次调用完毕后,将channel重新释放到队列中,以便下次复用
            NettyChannelPoolFactory.getInstance().release(blockingQueue, channel, inetSocketAddress);
        }
    }
}
