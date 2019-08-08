package com.simple.rpc.framework.balance.strategy.impl;

import com.simple.rpc.framework.balance.strategy.LoadBalanceStrategy;
import com.simple.rpc.framework.utils.IPHelper;
import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;

import java.util.List;

/**
 * 软负载hash算法实现
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class HashLoadBalanceStrategyImpl implements LoadBalanceStrategy {

    @Override
    public ProviderRegisterMessage select(List<ProviderRegisterMessage> providerServices) {
        // 获取调用方ip
        String localIP = IPHelper.localIp();
        // 获取源地址对应的hashcode
        int hashCode = localIP.hashCode();
        // 获取服务列表大小
        int size = providerServices.size();
        return providerServices.get(hashCode % size);
    }
}
