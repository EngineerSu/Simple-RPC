package com.simple.rpc.framework.provider;

import com.simple.rpc.framework.serialize.message.RequestMessage;
import com.simple.rpc.framework.serialize.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 利用反射调用服务
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class ServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProvider.class);

    public static ResponseMessage execute(RequestMessage request) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        // 创建接口实现类对象
        Object provider = Class.forName(request.getServiceImplPath()).newInstance();
        // 确定方法参数的Class列表,用于获取Method对象
        Class<?>[] parameterClasses = null;
        String[] parameterTypes = request.getParameterTypes();
        if (null != parameterTypes && parameterTypes.length > 0) {
            parameterClasses = new Class<?>[parameterTypes.length];
            for (int i = 0; i < request.getParameterTypes().length; i++) {
                try {
                    parameterClasses[i] = Class.forName(request.getParameterTypes()[i]);
                } catch (ClassNotFoundException e) {
                    LOGGER.error("未找到该方法参数的类型:" + request.getParameterTypes()[i]);
                    e.printStackTrace();
                    throw new ClassNotFoundException();
                }
            }
        }
        try {
            Method method = provider.getClass().getMethod(request.getMethodName(), parameterClasses);
            Object returnValue = method.invoke(provider, request.getParameters());
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setReturnValue(returnValue);
            responseMessage.setTraceId(request.getTraceId());
            responseMessage.setTimeout(request.getTimeout());
            return responseMessage;
        } catch (NoSuchMethodException e) {
            LOGGER.error("该方法不存在:" + request.getMethodName());
            throw new RuntimeException("反射调用服务时,发生错误");
        } catch (InvocationTargetException e) {
            throw new RuntimeException("反射调用服务时,发生错误");
        } catch (IllegalAccessException e) {
            LOGGER.error("无法访问该方法:" + request.getMethodName());
            throw new RuntimeException("反射调用服务时,发生错误");
        }
    }
}
