package com.simple.rpc.framework.zookeeper;

import com.simple.rpc.framework.zookeeper.message.InvokerRegisterMessage;

import java.util.List;
import java.util.Map;

/**
 * 服务治理接口
 *
 * @author liyebing created on 17/4/26.
 * @version $Id$
 */
public interface RegisterCenter4Governance {

    /**
     * 实时获取每个服务都有哪些调用者在使用,key:服务接口全限定名,value:调用者列表
     * 调用者信息目前主要就是machineID,代表该调用者所在机器的唯一标识
     */
    Map<String, List<InvokerRegisterMessage>> getInvokersOfProvider();

    /**
     * 实时获取每个调用者都调用了哪些服务,key:调用者machineID,value:服务者命名空间(appName/接口全限定名)列表
     */
    Map<String, List<String>> getProvidersOfInvoker();


}
