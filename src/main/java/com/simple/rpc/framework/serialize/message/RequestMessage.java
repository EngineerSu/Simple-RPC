package com.simple.rpc.framework.serialize.message;

import java.io.Serializable;

/**
 * 自定义请求消息
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class RequestMessage implements Serializable {

    /**
     * 消息id:消息的唯一标识
     */
    private String traceId;

    /*====从配置中读取的请求信息====*/
    /**
     * 服务接口的全限定名
     */
    private String servicePath;
    /**
     * 响应超时时间
     */
    private long timeout = 3000;

    /*====从反射中获取的请求信息====*/
    /**
     * 待执行方法名称
     */
    private String methodName;
    /**
     * 待执行方法的参数值
     */
    private Object[] parameters;
    /**
     * 待执行方法参数全限定名,用于反射时寻找method
     */
    private String[] parameterTypes;

    /*====从缓存注册地址中获取的服务方信息====*/
    /**
     * 服务端限流的信号量大小
     */
    private Integer workerThread;
    /**
     * 服务接口实现类的bean标签id(经过负载均衡后)
     */
    private String refId;

    public Integer getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(Integer workerThread) {
        this.workerThread = workerThread;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "RequestMessage{" +
                ", servicePath='" + servicePath + '\'' +
                ", serviceImplPath='" + refId + '\'' +
                ", methodName='" + methodName + '\'' +
                ", timeout=" + timeout +
                '}';
    }
}
