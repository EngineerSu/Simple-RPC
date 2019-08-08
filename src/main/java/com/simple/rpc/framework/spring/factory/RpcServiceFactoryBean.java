package com.simple.rpc.framework.spring.factory;

import com.simple.rpc.framework.provider.NettyServer;
import com.simple.rpc.framework.zookeeper.RegisterCenter;
import com.simple.rpc.framework.zookeeper.RegisterCenter4Provider;
import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import com.simple.rpc.framework.utils.IPHelper;
import com.simple.rpc.framework.utils.StartUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 接收vivo:service标签内容,每一个标签都会生成这个类的一个对象,通过这个对象完成服务端nettyserver的开启 和 初始化
 *
 * @author 11102342 suchang 2019/07/03
 */
public class RpcServiceFactoryBean implements FactoryBean, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RpcServiceFactoryBean.class);

    // 缓存已经开启服务的端口,避免重复开启服务和便于NettyServer实例的管理
    private static final Map<Integer, NettyServer> nettyServerMap = new HashMap<>();

    /*需要注册到zk中的信息*/
    /*必选*/
    // 接口所在应用名
    private String appName;
    // 服务接口
    private String servicePath;
    // 服务接口实现类对象(通过其获取实现类全限定名)
    private Object ref;
    // 服务端口
    private Integer serverPort;
    // 服务超时时间
    private long timeout;
    /*可选*/
    // 服务分组组名
    private String groupName = "default";
    // 服务提供者权重,范围为[1-100]
    private int weight = 1;
    // 服务端限流
    private int workerThreads = 10;


    // 因为服务端开启了代理Server就可以,而不用为每一个标签生成一个对象,所以这里暂时都为null
    @Override
    public Object getObject() throws Exception {
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 公用的初始化
//        StartUtils.loadClasses();
        // 组装标签信息
        ProviderRegisterMessage provider = new ProviderRegisterMessage();
        provider.setAppName(appName);
        provider.setServicePath(servicePath);
        provider.setServiceImplPath(ref.getClass().getName());
        // 获取本机ip地址
        provider.setServerIp(IPHelper.localIp());
//        provider.setServerIp(InetAddress.getLocalHost().getHostAddress());
        provider.setServerPort(serverPort);
        provider.setTimeout(timeout);
        // 以下都有默认值,如果标签内容有就是标签值,但是都要配置
        provider.setWorkerThread(workerThreads);
        provider.setWeight(weight);
        provider.setGroupName(groupName);
        // 注册服务到zk
        RegisterCenter4Provider registerCenter4Provider = RegisterCenter.getInstance();
        registerCenter4Provider.registerProvider(provider);

        // 发布代理服务
        long times = System.currentTimeMillis();
        NettyServer nettyServer = nettyServerMap.get(serverPort);
        // 如果缓存中没有这个端口,就开启服务
        if (null == nettyServer) {
            // 使用新的NettyServer开启服务,需要记录在本地缓存
            nettyServer = new NettyServer();
            nettyServer.start(serverPort);
            nettyServerMap.put(serverPort, nettyServer);
            times = System.currentTimeMillis() - times;
            logger.info("[{}]端口开启代理服务耗时{}ms", serverPort, times);
        }
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    public static Logger getLogger() {
        return logger;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public static Map<Integer, NettyServer> getNettyServerMap() {
        return nettyServerMap;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
