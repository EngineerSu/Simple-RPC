package com.simple.rpc.framework.balance.strategy;

import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;

import java.util.List;

/**
 * 负载均衡接口
 *
 * @author jacksu
 * @date 2018/8/8
 */
public interface LoadBalanceStrategy {

    /**
     * 负载策略方法:从服务地址列表中选择一个服务地址
     *
     * @param providerServices 服务地址列表
     * @return 被负载均衡算法选中的服务地址
     */
    ProviderRegisterMessage select(List<ProviderRegisterMessage> providerServices);
}
