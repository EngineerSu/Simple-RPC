package com.simple.rpc.framework.balance.strategy.impl;

import com.simple.rpc.framework.balance.common.LoadBalanceEngine;
import com.simple.rpc.framework.balance.strategy.LoadBalanceStrategy;
import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;

import java.util.List;

/**
 * 软负载随机算法实现
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class RandomLoadBalanceStrategyImpl implements LoadBalanceStrategy {

    @Override
    public ProviderRegisterMessage select(List<ProviderRegisterMessage> providerServices) {
        return LoadBalanceEngine.randomSelect(providerServices);
    }
}
