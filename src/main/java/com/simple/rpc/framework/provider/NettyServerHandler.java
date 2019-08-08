package com.simple.rpc.framework.provider;

import com.google.common.collect.Maps;
import com.simple.rpc.framework.serialize.message.RequestMessage;
import com.simple.rpc.framework.serialize.message.ResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 服务端代理类Handler
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RequestMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);

    /**
     * 服务端限流Map(每个服务限流信号量可以在rpc-service.xml的标签中设置)
     */
    private static final Map<String, Semaphore> SERVICE_KEY_SEMAPHORE_MAP = Maps.newConcurrentMap();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常,关闭链路
        ctx.close();
    }

    /**
     * 服务端与客户端成功建立连接后,调用反射完成服务
     *
     * @param ctx     Channel上下文
     * @param request 经过解码后得到的请求信息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestMessage request) throws RuntimeException {
        long startTime = System.currentTimeMillis();
        LOGGER.info("服务端接收请求消息:[content:{} id:{}]", request, request.getTraceId());
        if (ctx.channel().isWritable()) {
            long consumeTimeOut = request.getTimeout();
            // 根据方法名称定位到具体某一个服务提供者
            String serviceKey = request.getServiceImplPath();
            // 进行限流设置
            int workerThread = request.getWorkerThread();
            Semaphore semaphore = SERVICE_KEY_SEMAPHORE_MAP.get(serviceKey);
            if (null == semaphore) {
                synchronized (SERVICE_KEY_SEMAPHORE_MAP) {
                    semaphore = SERVICE_KEY_SEMAPHORE_MAP.get(serviceKey);
                    if (null == semaphore) {
                        semaphore = new Semaphore(workerThread);
                        SERVICE_KEY_SEMAPHORE_MAP.put(serviceKey, semaphore);
                    }
                }
            }
            ResponseMessage response = null;
            boolean acquire = false;
            try {
                // 利用semaphore实现限流
                acquire = semaphore.tryAcquire(consumeTimeOut, TimeUnit.MILLISECONDS);
                if (acquire) {
                    // 利用反射发起服务调用
                    response = ServiceProvider.execute(request);
                } else {
                    LOGGER.warn("服务限流,请求超时!");
                }
            } catch (Exception e) {
                LOGGER.error("服务方使用反射调用服务时发生错误", e);
                throw new RuntimeException("服务方使用反射调用服务时发生错误");
            } finally {
                if (acquire) {
                    // 恢复信号量
                    semaphore.release();
                }
            }
            if (null == response) {
                throw new RuntimeException("服务方使用反射调用服务时发生错误");
            }
            // 回写结果
            long times = System.currentTimeMillis() - startTime;
            LOGGER.info("服务端调用服务耗时{}ms", times);
            LOGGER.info("服务端发送返回结果:[content:{} id:{}]", response, response.getTraceId());
            ctx.writeAndFlush(response);
        } else {
            LOGGER.error("Channel发生异常关闭,请求失败!");
            throw new RuntimeException("Channel发生异常关闭,请求失败!");
        }
    }
}
