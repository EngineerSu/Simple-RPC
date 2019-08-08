package com.simple.rpc.framework.provider;

import com.simple.rpc.framework.serialize.handler.NettyDecoderHandler;
import com.simple.rpc.framework.serialize.handler.NettyEncoderHandler;
import com.simple.rpc.framework.serialize.message.RequestMessage;
import com.simple.rpc.framework.utils.PropertyConfigHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 服务端代理类:支持启动多个端口(多例)
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class NettyServer {

    /**
     * 服务端boss线程组
     */
    private EventLoopGroup bossGroup;
    /**
     * 服务端worker线程组
     */
    private EventLoopGroup workerGroup;
    /**
     * 绑定端口的Channel
     */
    private Channel channel;

    /**
     * 启动Netty服务
     */
    public void start(final int port) {
        synchronized (NettyServer.class) {
            if (bossGroup != null || workerGroup != null) {
                return;
            }
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            String serialize = PropertyConfigHelper.getServerSerializer();
            serverBootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 注册接收消息解码器
                            ch.pipeline().addLast(new NettyDecoderHandler(RequestMessage.class));
                            // 注册返回消息编码器
                            ch.pipeline().addLast(new NettyEncoderHandler(serialize));
                            // 注册服务端业务逻辑处理器
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    });
            try {
                channel = serverBootstrap.bind(port).sync().channel();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 停止Netty服务,可以在spring中配置为销毁方法
     */
    public void stop() {
        if (null == channel) {
            throw new RuntimeException("Netty Server Stoped");
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }
}
