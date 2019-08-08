package com.simple.rpc.framework.invoker;

import com.simple.rpc.framework.balance.common.LoadBalanceEngine;
import com.simple.rpc.framework.serialize.message.RequestMessage;
import com.simple.rpc.framework.serialize.message.ResponseMessage;
import com.simple.rpc.framework.utils.PropertyConfigHelper;
import com.simple.rpc.framework.zookeeper.RegisterCenter;
import com.simple.rpc.framework.zookeeper.RegisterCenter4Invoker;
import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * 客户端代理工厂:每个rpc服务接口引用的实际指向对象都由工厂的getProxyInstance方法产生
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class ClientProxyBeanFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientProxyBeanFactory.class);

    /**
     * 饿汉单例模式
     */
    private static ClientProxyBeanFactory instance = new ClientProxyBeanFactory();
    ;

    /**
     * 客户端的注册中心:获取服务地址列表
     */
    private static RegisterCenter4Invoker registerCenter4Invoker = RegisterCenter.getInstance();

    /**
     * 线程池:RPC服务调用都需要走此线程池中的线程(懒汉模式)
     */
    private static ExecutorService fixedThreadPool;

    /**
     * rpc-service.xml/rpc-reference.xml配置文件中标签属性默认值的等价字符串
     */
    private static final String DEFAULT_VALUE_IN_LABEL = "default";

    private ClientProxyBeanFactory() {
    }

    public static ClientProxyBeanFactory getInstance() {
        return instance;
    }

    /**
     * 生成引用服务的代理对象
     *
     * @param appName             应用名
     * @param serviceInterface    接口名(应用名 + 接口名是服务的key)
     * @param consumeTimeout      超时时间
     * @param loadBalanceStrategy 负载均衡策略
     * @param <T>                 引用服务的实际类型
     * @return 引用服务的代理对象
     */
    public static <T> T getProxyInstance(String appName, final Class<T> serviceInterface, int consumeTimeout, final String loadBalanceStrategy) {
        long startTime = System.currentTimeMillis();
        Object proxy = Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class[]{serviceInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 获取该接口的服务地址列表
                String nameSpace = appName + "/" + serviceInterface.getName();
                List<ProviderRegisterMessage> providerRegisterMessages = registerCenter4Invoker.getProviderMap().get(nameSpace);
                // 判断是否采用默认的负载均衡策略
                String strategy = loadBalanceStrategy;
                if (DEFAULT_VALUE_IN_LABEL.equalsIgnoreCase(strategy)) {
                    // 使用simple-rpc.properties配置的默认负载均衡策略
                    strategy = PropertyConfigHelper.getDefaultClusterStrategy();
                }
                // 根据负载均衡策略选择服务地址
                ProviderRegisterMessage providerRegisterMessage = LoadBalanceEngine.select(providerRegisterMessages, strategy);
                if (null == providerRegisterMessage) {
                    throw new RuntimeException("无可用的服务节点!");
                }
                // 设置消息内容
                RequestMessage request = new RequestMessage();
                // 消息ID(具有唯一性)
                request.setTraceId(UUID.randomUUID().toString());
                // 缓存服务地址和服务限流参数
                request.setServiceImplPath(providerRegisterMessage.getServiceImplPath());
                request.setWorkerThread(providerRegisterMessage.getWorkerThread());
                // 服务超时时间
                request.setTimeout(consumeTimeout);
                // 服务接口名称
                request.setServicePath(serviceInterface.getName());
                // 服务调用的方法名称
                request.setMethodName(method.getName());
                if (null != args && args.length > 0) {
                    // 设置方法参数和类型(类型用于反射得到Method对象)
                    request.setParameters(args);
                    request.setParameterTypes(new String[args.length]);
                    Type[] types = method.getGenericParameterTypes();
                    for (int i = 0; i < args.length; i++) {
                        // 因为寻找方法时,是根据方法参数的顶级type,所以当args参数是参数化类型时,需要寻找它的顶级type的类名
                        if (types[i] instanceof ParameterizedType) {
                            request.getParameterTypes()[i] = ((ParameterizedType) types[i]).getRawType().getTypeName();
                        } else {
                            request.getParameterTypes()[i] = types[i].getTypeName();
                        }
                    }
                }
                Future<ResponseMessage> responseMessage = null;
                try {
                    // 懒汉线程池
                    if (null == fixedThreadPool) {
                        synchronized (ClientProxyBeanFactory.class) {
                            if (null == fixedThreadPool) {
                                // 配置文件中设置的线程池数量
                                int corePoolSize = PropertyConfigHelper.getThreadWorkerNumber();
                                // 相当于固定数量的线程池,但是提供了容量100的队列供排队,超过了队列长度的请求直接丢弃
                                fixedThreadPool = new ThreadPoolExecutor(corePoolSize, corePoolSize, 0, TimeUnit.MILLISECONDS,
                                        new ArrayBlockingQueue<>(100), new DefaultThreadFactory("Simple-RPC-InvokerPool"), new ThreadPoolExecutor.DiscardPolicy());
                            }
                        }
                    }
                    // 组装服务地址
                    String serverIp = providerRegisterMessage.getServerIp();
                    int serverPort = providerRegisterMessage.getServerPort();
                    InetSocketAddress inetSocketAddress = new InetSocketAddress(serverIp, serverPort);
                    // 扔任务到线程池(任务包含的信息:地址+请求)
                    responseMessage = fixedThreadPool.submit(new RevokerServiceCallable(inetSocketAddress, request));
                    // 阻塞等待结果
                    ResponseMessage response = responseMessage.get(request.getTimeout(), TimeUnit.MILLISECONDS);
                    // 返回结果
                    return response.getReturnValue();
                } catch (InterruptedException ie) {
                    LOGGER.error("请求超时,线程已中断!");
                    responseMessage.cancel(true);
                }
                return null;
            }
        });
        long time = System.currentTimeMillis() - startTime;
        LOGGER.info("创建代理对象耗时{}ms:[{}]", time, serviceInterface.getName());
        return (T) proxy;
    }

}
