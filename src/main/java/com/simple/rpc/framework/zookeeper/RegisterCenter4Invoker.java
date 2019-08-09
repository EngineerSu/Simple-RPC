package com.simple.rpc.framework.zookeeper;

import com.simple.rpc.framework.zookeeper.message.InvokerRegisterMessage;
import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;

import java.util.List;
import java.util.Map;

/**
 * 消费端注册中心接口
 *
 * @author jacksu
 * @date 2018/8/8
 */
public interface RegisterCenter4Invoker {

    /**
     * 获取本地的服务列表
     */
    Map<String, List<ProviderRegisterMessage>> getProviderMap();

    /**
     * 注册服务使用者信息到ZK
     */
    List<ProviderRegisterMessage> registerInvoker(InvokerRegisterMessage invokerRegisterMessage);

}
