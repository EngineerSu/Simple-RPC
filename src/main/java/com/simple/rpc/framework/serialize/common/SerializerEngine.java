package com.simple.rpc.framework.serialize.common;


import com.simple.rpc.framework.serialize.serializer.HessianSerializer;
import com.simple.rpc.framework.serialize.serializer.ProtoStuffSerializer;
import com.simple.rpc.framework.serialize.serializer.Serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列化协议引擎:门面模式的门面类
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class SerializerEngine {

    /**
     * 缓存序列化接口实现类的单例对象
     */
    private static final Map<String, Serializer> SERIALIZER_MAP = new HashMap<>();

    // 可以认为是饿汉单例模式
    static {
        SERIALIZER_MAP.put(SerializerType.PROTO_STUFF, new ProtoStuffSerializer());
        SERIALIZER_MAP.put(SerializerType.HESSIAN, new HessianSerializer());
    }

    /**
     * 序列化
     *
     * @param t              待序列化对象
     * @param serializerType 序列化协议类型
     * @param <T>            泛型
     * @return 序列化对象得到的字节数组
     */
    public static <T> byte[] serialize(T t, String serializerType) {
        // 忽略大小写,如果配置错误,选择默认序列化协议
        String type = SerializerType.getValidType(serializerType);
        return SERIALIZER_MAP.get(type).serialize(t);
    }

    /**
     * 反序列化
     *
     * @param data           序列化得到的字节数组
     * @param clazz          对象类型
     * @param serializerType 序列化类型
     * @param <T>            泛型
     * @return 反序列化得到对象
     * @throws Exception
     */
    public static <T> T deserialize(byte[] data, Class<T> clazz, String serializerType) throws Exception {
        // 忽略大小写,如果配置错误,选择默认序列化协议
        String type = SerializerType.getValidType(serializerType);
        return SERIALIZER_MAP.get(type).deserialize(data, clazz);
    }
}
