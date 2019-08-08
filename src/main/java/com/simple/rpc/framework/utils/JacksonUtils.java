package com.simple.rpc.framework.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Jackson工具类,用于消息编解码器的开发
 *
 * @author 11102342 suchang 2019/6/27
 */
public class JacksonUtils {

    // 定义jackson对象
    private static ObjectMapper MAPPER = new ObjectMapper();

    // 将对象转换成btye数组
    public static byte[] objectToBytes(Object obj) {
        try {
            return MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            System.out.println("objectToBytes error: json解析异常");
            e.printStackTrace();
        }
        return null;
    }

    // 对于简单对象来说,type字符串就是其类的全限定名,如java.lang.String;对于复杂列表对象来说,type字符串例子:java.util.Map<java.lang.String, java.util.List<test.Person>>
    public static Object bytesToObject(byte[] bytes, String type) {
        try {
            JavaType javaType = MAPPER.getTypeFactory().constructFromCanonical(type);
            Object o = MAPPER.readValue(bytes, javaType);
            return o;
        } catch (JsonParseException e) {
            e.printStackTrace();
            System.out.println("bytesToObject error: JsonParseException");
        } catch (JsonMappingException e) {
            e.printStackTrace();
            System.out.println("bytesToObject error: JsonMappingException");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("bytesToObject error: IOException");
        }
        return null;
    }

    // beanType字符串只能是简单对象的class对象,对于列表对象不能使用这个方法
    public static <T> T bytesToObject(byte[] bytes, Class<T> beanType) {
        try {
            T t = MAPPER.readValue(bytes, beanType);
            return t;
        } catch (JsonParseException e) {
            e.printStackTrace();
            System.out.println("json字符串解析异常");
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 将对象转换成json字符串
    public static String objectToJson(Object obj) {
        try {
            String str = MAPPER.writeValueAsString(obj);
            return str;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 将json数据转换成pojo对象
    public static <T> T jsonToObject(String json, Class<T> beanType) {
        try {
            T t = MAPPER.readValue(json, beanType);
            return t;
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
