package com.simple.rpc.framework.serialize.handler;

import com.simple.rpc.framework.serialize.common.SerializerEngine;
import com.simple.rpc.framework.serialize.common.SerializerType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 解码器Handler
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class NettyDecoderHandler extends ByteToMessageDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyDecoderHandler.class);

    /**
     * 解码类的Class对象
     */
    private Class<?> genericClass;

    /**
     * 只提供此构造方法:必须指定反解码的类型
     */
    public NettyDecoderHandler(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        long time = System.currentTimeMillis();
        // 头部消息8字节:序列化协议int + 数据长度int
        if (in.readableBytes() < 8) {
            return;
        }
        in.markReaderIndex();
        int serializerCode = in.readInt();
        String serializerType = SerializerType.getTypeByCode(serializerCode);
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        // 若当前可以获取到的字节数小于实际长度,则直接返回,直到当前可以获取到的字节数等于实际长度
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        // 读取完整的消息体字节数组
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        // 将字节数组反序列化为java对象(SerializerEngine参考序列化与反序列化章节)
        Object obj = SerializerEngine.deserialize(data, genericClass, serializerType);
        out.add(obj);
        time = System.currentTimeMillis() - time;
        LOGGER.info("[{}]协议解码耗时{}ms", serializerType, time);
    }

}
