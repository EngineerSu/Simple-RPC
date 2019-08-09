package com.simple.rpc.framework.zookeeper;

import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;

/**
 * 服务端注册中心接口
 *
 * @author jacksu
 * @date 2018/8/8
 */
public interface RegisterCenter4Provider {

    /**
     * 注册服务到ZK
     */
    void registerProvider(ProviderRegisterMessage providerRegisterMessage);
}
