package com.simple.rpc.framework.serialize.serializer;

/**
 * 序列化协议接口
 *
 * @author jacksu
 * @date 2018/8/8
 */
public interface Serializer {

    /**
     * 序列化
     *
     * @param t 待序列化的对象
     * @return 序列化对象得到的字节数组
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化
     *
     * @param data  待反序列化的字节数组
     * @param clazz 反序列化需要指定Class对象
     * @return 反序列化得到的对象
     * @throws Exception 可能抛出异常
     */
    <T> T deserialize(byte[] data, Class<T> clazz) throws Exception;
}
