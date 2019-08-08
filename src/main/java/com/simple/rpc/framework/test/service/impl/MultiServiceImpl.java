package com.simple.rpc.framework.test.service.impl;

import com.simple.rpc.framework.test.service.MultiService;

/**
 * 测试RPC服务接口实现类
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class MultiServiceImpl implements MultiService {

    @Override
    public String sayHi(String name) throws InterruptedException {
        return String.format("[MultiService] Hi,%s. Are you OK?", name);
    }

    @Override
    public void sayHi() {
        System.out.println("[MultiService] Hi,Simple-RPC!");
    }

}
