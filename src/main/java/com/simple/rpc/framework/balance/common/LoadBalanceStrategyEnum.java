package com.simple.rpc.framework.balance.common;

import org.apache.commons.lang3.StringUtils;

/**
 * 负载均衡引擎枚举策略
 *
 * @author jacksu
 * @date 2018/8/8
 */
public enum LoadBalanceStrategyEnum {

    // 随机算法
    Random("Random"),
    // 权重随机算法
    WeightRandom("WeightRandom"),
    // 轮询算法
    Polling("Polling"),
    // 权重轮询算法
    WeightPolling("WeightPolling"),
    // 源地址hash算法
    Hash("Hash");

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    LoadBalanceStrategyEnum(String code) {
        this.code = code;
    }

    public static LoadBalanceStrategyEnum queryByCode(String code) {
        if (null == code || StringUtils.isBlank(code)) {
            return null;
        }
        for (LoadBalanceStrategyEnum strategy : values()) {
            // 因为是从自定义标签取的值,所以不区分大小写,提高可用性
            if (StringUtils.equalsIgnoreCase(code, strategy.getCode())) {
                return strategy;
            }
        }
        return null;
    }

}
