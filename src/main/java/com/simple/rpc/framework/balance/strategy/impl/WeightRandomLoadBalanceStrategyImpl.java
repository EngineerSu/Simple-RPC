package com.simple.rpc.framework.balance.strategy.impl;

import com.simple.rpc.framework.balance.common.LoadBalanceEngine;
import com.simple.rpc.framework.balance.strategy.LoadBalanceStrategy;
import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * 软负载加权随机算法实现
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class WeightRandomLoadBalanceStrategyImpl implements LoadBalanceStrategy {

    @Override
    public ProviderRegisterMessage select(List<ProviderRegisterMessage> providerServices) {
        // 根据加权创建服务列表索引:加权为3,则它的索引在这个数组中出现三次
        List<Integer> indexList = LoadBalanceEngine.getIndexListByWeight(providerServices);
        int index = RandomUtils.nextInt(0, indexList.size());
        return providerServices.get(indexList.get(index));
    }


}
