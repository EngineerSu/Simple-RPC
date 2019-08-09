package com.simple.rpc.framework.spring.factory;

import com.google.common.collect.Sets;
import com.simple.rpc.framework.invoker.ClientProxyBeanFactory;
import com.simple.rpc.framework.invoker.NettyChannelPoolFactory;
import com.simple.rpc.framework.zookeeper.RegisterCenter;
import com.simple.rpc.framework.zookeeper.message.InvokerRegisterMessage;
import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;


/**
 * 接收simple:reference标签内容,每一个标签都会生成这个类的一个对象,通过这个对象生成rpc服务接口的代理对象并完成初始化
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class RpcReferenceFactoryBean implements FactoryBean, InitializingBean {

    /**
     * 缓存的服务地址集合(ip+port认为是一个地址)
     */
    private static Set<InetSocketAddress> socketAddressSet = Sets.newHashSet();

    /**
     * ChannelPool工厂
     */
    private static NettyChannelPoolFactory nettyChannelPoolFactory = NettyChannelPoolFactory.getInstance();

    /**
     * 注册中心
     */
    private static RegisterCenter registerCenter = RegisterCenter.getInstance();

    /*simple:reference标签中必须的属性参数*/
    /**
     * 服务接口
     */
    private Class<?> targetInterface;
    /**
     * 超时时间
     */
    private int timeout;
    /**
     * 服务所属应用名
     */
    private String appName;

    /*simple:reference标签中可选的属性参数*/
    /**
     * 服务分组组名(本项目没用,可以自行在注册中心中拓展)
     */
    private String groupName = "default";

    /*本地使用参数(不需要传到ZK)*/
    /**
     * 负载均衡策略
     */
    private String clusterStrategy = "default";

    /**
     * invoker的初始化: 获取引用服务的远程地址 / 新的远程地址会生成一定数量的channel到channelpool中
     */
    @Override
    public void afterPropertiesSet() {
        // 将标签内容注册到zk中,同时获取标签内容的服务地址到本地
        InvokerRegisterMessage invoker = new InvokerRegisterMessage();
        invoker.setServicePath(targetInterface.getName());
        invoker.setGroupName(groupName);
        invoker.setAppName(appName);
        // 本机所有invoker的machineID是一样的
        invoker.setInvokerMachineID4Server(InvokerRegisterMessage.getInvokerMachineID4Client());
        // 根据标签内容从注册中心获取的地址
        List<ProviderRegisterMessage> providerRegisterMessages = registerCenter.registerInvoker(invoker);
        // 提前为不同的主机地址创建ChannelPool
        for (ProviderRegisterMessage provider : providerRegisterMessages) {
            InetSocketAddress socketAddress = new InetSocketAddress(provider.getServerIp(), provider.getServerPort());
            boolean firstAdd = socketAddressSet.add(socketAddress);
            if (firstAdd) {
                nettyChannelPoolFactory.registerChannelQueueToMap(socketAddress);
            }
        }
    }

    /**
     * 生成simple:reference标签引用服务接口的代理对象
     *
     * @return 引用服务接口的代理对象
     */
    @Override
    public Object getObject() {
        return ClientProxyBeanFactory.getProxyInstance(appName, targetInterface, timeout, clusterStrategy);
    }

    /**
     * 声明接口代理对象的类型
     *
     * @return
     */
    @Override
    public Class<?> getObjectType() {
        return targetInterface;
    }

    /**
     * 声明是否单例
     *
     * @return
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<?> getTargetInterface() {
        return targetInterface;
    }

    public void setTargetInterface(Class<?> targetInterface) {
        this.targetInterface = targetInterface;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getClusterStrategy() {
        return clusterStrategy;
    }

    public void setClusterStrategy(String clusterStrategy) {
        this.clusterStrategy = clusterStrategy;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
