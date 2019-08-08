package com.simple.rpc.framework.serialize.serializer;

/**
 * 序列化协议公共接口
 *
 * @author 11102342 suchang 2019/07/05
 */
public interface Serializer {

    /**
     * 序列化
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化
     */
    <T> T deserialize(byte[] data, Class<T> clazz) throws Exception;
}
