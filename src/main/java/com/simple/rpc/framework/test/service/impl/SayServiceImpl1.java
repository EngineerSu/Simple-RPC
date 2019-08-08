package com.simple.rpc.framework.test.service.impl;

import com.simple.rpc.framework.test.service.SayService;

/**
 * 测试RPC服务接口实现类1
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class SayServiceImpl1 implements SayService {

    @Override
    public void saySlogan() {
        System.out.println("[SayService1] Hello Simple-RPC!");
    }

    @Override
    public String sayHi(String name) {
        return String.format("[SayService1] Hi,%s. Are you OK?", name);
    }
}
