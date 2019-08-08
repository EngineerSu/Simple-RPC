package com.simple.rpc.framework.test.service.impl;

import com.simple.rpc.framework.test.service.SayService;

/**
 * 测试RPC服务接口实现类2
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class SayServiceImpl2 implements SayService {

    @Override
    public void saySlogan() {
        System.out.println("[SayService2] Hello Simple-RPC!");
    }

    @Override
    public String sayHi(String name) {
        return String.format("[SayService2] Hi,%s. Are you OK?", name);
    }
}
