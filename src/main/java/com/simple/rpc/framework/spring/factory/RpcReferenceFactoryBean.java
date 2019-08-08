package com.simple.rpc.framework.spring.factory;

import com.google.common.collect.Sets;
import com.simple.rpc.framework.invoker.ClientProxyBeanFactory;
import com.simple.rpc.framework.invoker.NettyChannelPoolFactory;
import com.simple.rpc.framework.utils.StartUtils;
import com.simple.rpc.framework.zookeeper.RegisterCenter;
import com.simple.rpc.framework.zookeeper.message.InvokerRegisterMessage;
import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

/**
 * 接收vivo:reference标签内容,每一个标签都会生成这个类的一个对象,通过这个对象生成rpc服务接口的代理对象并完成初始化
 *
 * @author 11102342 suchang 2019/07/03
 */
public class RpcReferenceFactoryBean implements FactoryBean, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RpcReferenceFactoryBean.class);

    // provider地址集合,为每一个地址提前创建一定数量的channel,但是不能重复创建
    private static Set<InetSocketAddress> socketAddressSet = Sets.newHashSet();

    // 单例channelpool工厂
    private static NettyChannelPoolFactory nettyChannelPoolFactory = NettyChannelPoolFactory.getInstance();

    // 单例注册中心
    private static RegisterCenter registerCenter = RegisterCenter.getInstance();

    /*必选的参数*/
    //服务接口
    private Class<?> targetInterface;
    //超时时间
    private int timeout;
    private String appName;

    /*可选参数*/
    //服务分组组名
    private String groupName = "default";

    /*本地使用参数(不需要传到ZK)*/
    //负载均衡策略
    private String clusterStrategy = "default";

    /**
     * invoker的初始化: 获取引用服务的远程地址 / 新的远程地址会生成一定数量的channel到channelpool中
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 公用的初始化
//        StartUtils.loadClasses();

        // 将标签内容注册到zk中,同时获取标签内容的服务地址到本地
        InvokerRegisterMessage invoker = new InvokerRegisterMessage();
        invoker.setServicePath(targetInterface.getName());
        invoker.setGroupName(groupName);
        invoker.setAppName(appName);
        // 本机所有invoker的machineID是一样的
        invoker.setInvokerMachineID4Server(InvokerRegisterMessage.getInvokerMachineID4Client());

        // 根据标签内容从注册中心获取的地址
        List<ProviderRegisterMessage> providerRegisterMessages = registerCenter.registerInvoker(invoker);
        // 分析地址获得主机,提前为主机建立channel
        for (ProviderRegisterMessage provider:providerRegisterMessages) {
            InetSocketAddress socketAddress = new InetSocketAddress(provider.getServerIp(), provider.getServerPort());
            boolean firstAdd = socketAddressSet.add(socketAddress);
            if (firstAdd) {
                nettyChannelPoolFactory.registerChannelQueueToMap(socketAddress);
            }
        }
    }

    @Override
    public Object getObject() throws Exception {
        return ClientProxyBeanFactory.getProxyInstance(appName, targetInterface, timeout, clusterStrategy);
    }

    @Override
    public Class<?> getObjectType() {
        return targetInterface;
    }

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
