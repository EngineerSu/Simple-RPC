package com.simple.rpc.framework.serialize.message;

import java.io.Serializable;

/**
 * 自定义响应消息
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class ResponseMessage implements Serializable {

    /**
     * 消息id:消息的唯一标识,与请求消息保持一致
     */
    private String traceId;

    /**
     * 存储方法执行结果
     */
    private Object returnValue;

    /**
     * 响应超时时间
     */
    private long timeout;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "traceId='" + traceId + '\'' +
                ", returnValue=" + returnValue +
                ", timeout=" + timeout +
                '}';
    }
}
