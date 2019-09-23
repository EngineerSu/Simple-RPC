package com.simple.rpc.framework.invoker;

import com.simple.rpc.framework.serialize.message.ResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端ChannelHandler
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<ResponseMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientHandler.class);

    public NettyClientHandler() { }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ResponseMessage response) throws Exception {
        // Netty异步获取结果后的操作:存入结果阻塞队列
        ResponseReceiverHolder.putResultValue(response);
        LOGGER.info("客户端接收返回结果:[content:{} id:{}]", response, response.getTraceId());
    }


}
