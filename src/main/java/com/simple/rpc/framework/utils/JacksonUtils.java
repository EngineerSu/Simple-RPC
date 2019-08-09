package com.simple.rpc.framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Jackson工具类
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class JacksonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonUtils.class);

    /**
     * 定义jackson对象
     */
    private static ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 将对象转换成字节数组
     */
    public static byte[] objectToBytes(Object obj) {
        try {
            return MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            LOGGER.error("objectToBytes error:json解析异常");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将字节数组转换成对象
     * 对于简单对象来说,type就是其类的全限定名,如java.lang.String;对于复杂列表对象来说,type例子:java.util.Map<java.lang.String, java.util.List<test.Person>>
     */
    public static Object bytesToObject(byte[] bytes, String type) {
        try {
            JavaType javaType = MAPPER.getTypeFactory().constructFromCanonical(type);
            Object o = MAPPER.readValue(bytes, javaType);
            return o;
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("bytesToObject error");
        }
        return null;
    }

    /**
     * 将字节数组变成对象
     * beanType字符串只能是简单对象的class对象,对于列表对象不能使用这个方法
     */
    public static <T> T bytesToObject(byte[] bytes, Class<T> beanType) {
        try {
            T t = MAPPER.readValue(bytes, beanType);
            return t;
        } catch (IOException e) {
            LOGGER.error("bytesToObject error");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将对象转换成json字符串
     */
    public static String objectToJson(Object obj) {
        try {
            String str = MAPPER.writeValueAsString(obj);
            return str;
        } catch (JsonProcessingException e) {
            LOGGER.error("objectToJson error");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json字符串转换成对象
     */
    public static <T> T jsonToObject(String json, Class<T> beanType) {
        try {
            T t = MAPPER.readValue(json, beanType);
            return t;
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("jsonToObject error");
        }
        return null;
    }
}
