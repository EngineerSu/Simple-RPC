package com.simple.rpc.framework.serialize.handler;

import com.simple.rpc.framework.serialize.common.SerializerEngine;
import com.simple.rpc.framework.serialize.common.SerializerType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 编码器Handler:序列化协议根据配置文件决定
 *
 * @author 11102342 suchang 2019/07/05
 */
public class NettyEncoderHandler extends MessageToByteEncoder {

    private static final Logger logger = LoggerFactory.getLogger(NettyDecoderHandler.class);
    private String serializerType;

    /**
     * 只提供此构造方法:必须提供序列化协议参数
     */
    public NettyEncoderHandler(String serializerType) {
        this.serializerType = serializerType;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        long time = System.currentTimeMillis();
        // 确定序列化协议的int码
        int serializerCode = SerializerType.getCodeByType(serializerType);
        out.writeInt(serializerCode);
        // 将对象序列化为字节数组
        byte[] data = SerializerEngine.serialize(in, serializerType);
        // 将字节数组(消息体)的长度作为消息头写入,解决半包/粘包问题
        out.writeInt(data.length);
        // 最后才写入序列化后得到的字节数组
        out.writeBytes(data);
        time = System.currentTimeMillis() - time;
        logger.info("[{}]协议编码耗时{}ms", SerializerType.getValidType(serializerType), time);
    }

    public String getSerializerType() {
        return serializerType;
    }

    public void setSerializerType(String serializerType) {
        this.serializerType = serializerType;
    }
}
