package com.simple.rpc.framework.zookeeper;

import com.simple.rpc.framework.zookeeper.message.InvokerRegisterMessage;
import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;

import java.util.List;
import java.util.Map;

/**
 * 消费端注册中心接口(功能)
 *
 * @author suchang created on 2019/06/28
 */
public interface RegisterCenter4Invoker {

    /**
     * 客户端启动时,根据配置文件去订阅服务,获取服务列表到本地
     * @param invokerRegisterMessages
     */
    void initInvokers(List<InvokerRegisterMessage> invokerRegisterMessages);

    /**
     * 获取本地的服务列表
     * @return
     */
    Map<String, List<ProviderRegisterMessage>> getProviderMap();

    /**
     * 引入配置文件一个标签的服务,同时注册自己的信息到zookeeper
     * @param invokerRegisterMessage
     */
    List<ProviderRegisterMessage> registerInvoker(InvokerRegisterMessage invokerRegisterMessage);

}
