package com.simple.rpc.framework.test.service;

/**
 * 测试RPC服务接口
 *
 * @author jacksu
 * @date 2018/8/8
 */
public interface MultiService {

    /**
     * 测试方法:打印对name的打招呼信息
     */
    String sayHi(String name) throws InterruptedException;

    /**
     * 测试方法:返回值为void
     */
    void sayHi();
}
