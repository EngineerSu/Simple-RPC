package com.simple.rpc.framework.test.service;

/**
 * 测试RPC服务接口
 *
 * @author jacksu
 * @date 2018/8/8
 */
public interface SayService {

    /**
     * 测试方法:打印固定消息
     */
    void saySlogan();

    /**
     * 测试方法:打印对name的打招呼信息
     */
    String sayHi(String name);

}
