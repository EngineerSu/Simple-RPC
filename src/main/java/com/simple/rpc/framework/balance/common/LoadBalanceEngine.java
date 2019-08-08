package com.simple.rpc.framework.balance.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.simple.rpc.framework.balance.strategy.*;
import com.simple.rpc.framework.balance.strategy.impl.*;
import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡引擎
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class LoadBalanceEngine {

    /**
     * 缓存负载均衡接口实现类对象的Map(调用负载均衡时,同一个策略都是用的同一个对象,相当于单例模式)
     */
    private static final Map<LoadBalanceStrategyEnum, LoadBalanceStrategy> STRATEGY_MAP = Maps.newConcurrentMap();

    // 饿汉单例模式
    static {
        STRATEGY_MAP.put(LoadBalanceStrategyEnum.Random, new RandomLoadBalanceStrategyImpl());
        STRATEGY_MAP.put(LoadBalanceStrategyEnum.WeightRandom, new WeightRandomLoadBalanceStrategyImpl());
        STRATEGY_MAP.put(LoadBalanceStrategyEnum.Polling, new PollingLoadBalanceStrategyImpl());
        STRATEGY_MAP.put(LoadBalanceStrategyEnum.WeightPolling, new WeightPollingLoadBalanceStrategyImpl());
        STRATEGY_MAP.put(LoadBalanceStrategyEnum.Hash, new HashLoadBalanceStrategyImpl());
    }

    /**
     * 根据方法参数调用实际的负载均衡策略对象,进行负载均衡选择
     *
     * @param clusterStrategy 用户配置的负载均衡策略
     * @return 被选中服务地址
     */
    public static ProviderRegisterMessage select(List<ProviderRegisterMessage> providerRegisterMessages, String clusterStrategy) {
        if (null == providerRegisterMessages || providerRegisterMessages.size() == 0) {
            return null;
        } else if (providerRegisterMessages.size() == 1) {
            return providerRegisterMessages.get(0);
        }
        LoadBalanceStrategyEnum loadBalanceStrategyEnum = LoadBalanceStrategyEnum.queryByCode(clusterStrategy);
        if (null == loadBalanceStrategyEnum) {
            // 默认选择随机算法
            return STRATEGY_MAP.get(LoadBalanceStrategyEnum.Random).select(providerRegisterMessages);
        }
        return STRATEGY_MAP.get(loadBalanceStrategyEnum).select(providerRegisterMessages);
    }

    /*下面是通用的方法,供负载均衡实现类使用(相当于工具类)*/
    /**
     * 根据权重值,获取服务地址的索引列表(服务权重值为多少,它的索引就会在列表中出现多少次)
     *
     * @param providerServices 服务地址列表
     * @return 索引列表
     */
    public static List<Integer> getIndexListByWeight(List<ProviderRegisterMessage> providerServices) {
        if (null == providerServices | providerServices.size() == 0) {
            return null;
        }
        ArrayList<Integer> list = Lists.newArrayList();
        int index = 0;
        for (ProviderRegisterMessage each : providerServices) {
            int weight = each.getWeight();
            while (weight-- > 0) {
                list.add(index);
            }
            index++;
        }
        return list;
    }

    /**
     * 随机算法
     *
     * @param providerServices 服务地址列表
     * @return 随机选中的服务地址
     */
    public static ProviderRegisterMessage randomSelect(List<ProviderRegisterMessage> providerServices) {
        int index = RandomUtils.nextInt(0, providerServices.size());
        return providerServices.get(index);
    }

}
