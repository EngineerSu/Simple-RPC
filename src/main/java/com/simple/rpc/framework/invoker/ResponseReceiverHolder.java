package com.simple.rpc.framework.invoker;

import com.google.common.collect.Maps;
import com.simple.rpc.framework.serialize.message.ResponseMessage;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 返回结果包装类容器:缓存客户端所有RPC服务调用的返回结果阻塞队列,并能清除过期返回结果
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class ResponseReceiverHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseReceiverHolder.class);

    /**
     * 缓存返回结果包装类的Map:key是请求id,value是返回结果封装类
     */
    private static final Map<String, ResponseReceiver> responseMap = Maps.newConcurrentMap();
    /**
     * 清除过期结果的线程池(模拟SingleThreadExecutor)
     */
    private static final ExecutorService removeExpireKeyExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1), new DefaultThreadFactory("removeExpireKeyExecutor"));

    // 删除超时未获取到结果的key,防止内存泄露
    static {
        removeExpireKeyExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        for (Map.Entry<String, ResponseReceiver> entry : responseMap.entrySet()) {
                            boolean isExpire = entry.getValue().isExpire();
                            if (isExpire) {
                                responseMap.remove(entry.getKey());
                            }
                            Thread.sleep(10);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 为请求创建一个返回结果包装类,并缓存到Mao中
     */
    public static void initResponseData(String traceId) {
        responseMap.put(traceId, new ResponseReceiver());
    }


    /**
     * 将Netty异步返回结果放入阻塞队列
     */
    public static void putResultValue(ResponseMessage response) {
        ResponseReceiver responseReceiver = responseMap.get(response.getTraceId());
        if (null == responseReceiver) {
            responseReceiver = new ResponseReceiver();
            responseMap.put(response.getTraceId(), responseReceiver);
        }
        responseReceiver.setResponseTime(System.currentTimeMillis());
        responseReceiver.getResponseQueue().add(response);
    }


    /**
     * 从阻塞队列中获取Netty异步返回的结果值
     */
    public static ResponseMessage getValue(String traceId, long timeout) throws InterruptedException {
        ResponseReceiver responseReceiver = responseMap.get(traceId);
        try {
            // 阻塞Queue在取值时会阻塞当前线程(等待),timeout时间后还未取到值,则发生中断异常
            return responseReceiver.getResponseQueue().poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("结果队列取值超时,线程已中断!");
            throw new InterruptedException();
        } finally {
            // 无论取没取到,本次请求已经处理过了,所以不需要再缓存它的结果
            responseMap.remove(traceId);
        }
    }

}
