package com.simple.rpc.framework.zookeeper;

import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;

import java.util.List;

/**
 * 服务端注册中心接口(功能)
 *
 * @author suchang created on 2019/06/28
 */
public interface RegisterCenter4Provider {


    /**
     * 启动服务端时,注册服务信息
     * @param providerRegisterMessages
     */
    void initProviders(List<ProviderRegisterMessage> providerRegisterMessages);

    /**
     * 注册一个服务到zk(一个标签对应一个服务)
     * @param providerRegisterMessage
     */
    void registerProvider(ProviderRegisterMessage providerRegisterMessage);
}
