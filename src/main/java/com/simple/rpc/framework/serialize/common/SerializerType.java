package com.simple.rpc.framework.serialize.common;

/**
 * 支持的序列化协议
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class SerializerType {

    /**
     * 代表序列化协议的字符串:用于SerializerEngine中SerializerMap的key
     */
    public static final String PROTO_STUFF = "ProtoStuff";
    public static final String HESSIAN = "Hessian";
    /**
     * 代表序列化协议的数字:用于编码时将序列化协议作为一个int携带在数据流中
     */
    public static final int PROTO_STUFF_CODE = 0;
    public static final int HESSIAN_CODE = 1;

    /**
     * 保证配置文件大小写兼容和容错性
     */
    public static String getValidType(String serializerType) {
        if (HESSIAN.equalsIgnoreCase(serializerType)) {
            return HESSIAN;
        } else if (PROTO_STUFF.equalsIgnoreCase(serializerType)) {
            return PROTO_STUFF;
        } else {
            // 默认值
            return PROTO_STUFF;
        }
    }

    /**
     * 序列化时,需要将采用的协议变成int存储在发生信息的头部
     */
    public static int getCodeByType(String serializerType) {
        if (HESSIAN.equalsIgnoreCase(serializerType)) {
            return HESSIAN_CODE;
        } else if (PROTO_STUFF.equalsIgnoreCase(serializerType)) {
            return PROTO_STUFF_CODE;
        } else {
            return PROTO_STUFF_CODE;
        }
    }

    /**
     * 反序列化时根据头部的int信息确定序列化协议
     */
    public static String getTypeByCode(int serializerCode) {
        if (HESSIAN_CODE == serializerCode) {
            return HESSIAN;
        } else if (PROTO_STUFF_CODE == serializerCode) {
            return PROTO_STUFF;
        } else {
            return PROTO_STUFF;
        }
    }
}
