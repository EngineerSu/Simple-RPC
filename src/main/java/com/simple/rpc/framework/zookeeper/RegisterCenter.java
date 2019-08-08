package com.simple.rpc.framework.zookeeper;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.simple.rpc.framework.zookeeper.message.InvokerRegisterMessage;
import com.simple.rpc.framework.zookeeper.message.ProviderRegisterMessage;
import com.simple.rpc.framework.utils.JacksonUtils;
import com.simple.rpc.framework.utils.PropertyConfigHelper;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 注册中心实现
 *
 * @author suchang created on 2019/06/28
 */
public class RegisterCenter implements RegisterCenter4Provider, RegisterCenter4Invoker, RegisterCenter4Governance {

    private static final Logger logger = LoggerFactory.getLogger(RegisterCenter.class);

    private static RegisterCenter instance = new RegisterCenter(); // 单例模式,懒汉模式

    // provider列表,Key:服务提供者接口的全限定名,value:服务提供者注册信息列表
    private static final Map<String, List<ProviderRegisterMessage>> providerMap = Maps.newConcurrentMap();

    // invkoer列表,key:服务提供者接口的全限定名,value:服务使用者信息列表(服务治理,先不做)
    private static final Map<String, List<InvokerRegisterMessage>> invokerMap = Maps.newConcurrentMap();

    // invoker节点的监听器Set,避免重复添加监听器
    private static final Set<String> invokerNodeListenerSet = Sets.newConcurrentHashSet();

    private static String ZK_SERVICE = PropertyConfigHelper.getZkService();
    private static int ZK_SESSION_TIME_OUT = PropertyConfigHelper.getZkSessionTimeout();
    private static int ZK_CONNECTION_TIME_OUT = PropertyConfigHelper.getZkConnectionTimeout();
    // 规定所有应用节点都在ROOT_PATH节点下
    private static String ROOT_PATH = "/com/simple/rpc/framework/zookeeper/vivorpc";
    public static String PROVIDER_TYPE = "com/simple/rpc/framework/provider";
    public static String INVOKER_TYPE = "com/simple/rpc/framework/invoker";
    private static volatile ZkClient zkClient = null; // 单例模式,饿汉模式

    private RegisterCenter() {
    }

    public static RegisterCenter getInstance() {
        return instance;
    }

    /**
     * 注册ROOT_PATH路径
     */
    static {
        // 发起zookeeper连接
        if (null == zkClient) {
            zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
        }
        // 注册根路径
        boolean exist = zkClient.exists(ROOT_PATH);
        if (!exist) {
            zkClient.createPersistent(ROOT_PATH, true);
        }
    }

    // 批量注册服务,标签初始化是单个服务注册,所以这个方法在本项目中暂时没用到
    @Override
    public void initProviders(List<ProviderRegisterMessage> providerRegisterMessages) {
        long startTime = System.currentTimeMillis();
        if (null == providerRegisterMessages || providerRegisterMessages.size() == 0) {
            return;
        }

        // 防止多次注册,需要锁(内部实际注册时,还有锁,双重锁)
        synchronized (RegisterCenter.class) {
            for (ProviderRegisterMessage provider : providerRegisterMessages) {
                registerProvider(provider);
            }
        }
        long times = System.currentTimeMillis() - startTime;
        logger.info("向zookeeper中批量注册服务成功,耗时{}ms", times);
    }

    /**
     * 注册单个服务
     */
    @Override
    public void registerProvider(ProviderRegisterMessage provider) {
        long startTime = System.currentTimeMillis();
        // 创建服务接口的命名空间
        String nameSpace = provider.getAppName() + "/" + provider.getServicePath();
        synchronized (RegisterCenter.class) {
            // ROOT_PATH/应用名/接口全限定名/provider(持久节点)
            String providerPath = ROOT_PATH + "/" + nameSpace + "/" + PROVIDER_TYPE;
            // 创建服务接口/provider的永久节点
            if (!zkClient.exists(providerPath)) {
                zkClient.createPersistent(providerPath, true);
            }
            // 注册服务信息(临时节点)
            String serviceMsgNode = providerPath + "/" + JacksonUtils.objectToJson(provider);
            // 创建临时节点(临时节点才能自动监听)
            if (!zkClient.exists(serviceMsgNode)) {
                zkClient.createEphemeral(serviceMsgNode);
            }
            // 创建服务接口/invoker的永久节点
            String invokerPath = ROOT_PATH + "/" + nameSpace + "/" + INVOKER_TYPE;
            if (!zkClient.exists(invokerPath)) {
                zkClient.createPersistent(invokerPath);
            }
            boolean firstAdd = invokerNodeListenerSet.add(invokerPath);
            // 为服务/consumer节点注册监听器,便于服务提供方获取该服务下的使用者(避免重复监听)
            if (firstAdd) {
                zkClient.subscribeChildChanges(invokerPath, new IZkChildListener() {
                    @Override
                    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                        // 服务接口的全限定名
                        if (null == currentChilds || currentChilds.size() == 0) {
                            invokerMap.remove(nameSpace);
                            logger.warn("[{}]节点发生变化,该服务节点下已无调用者", parentPath);
                            return;
                        }
                        // 监听到变化后invokerPath节点下的所有临时节点值是currentChilds
                        // 根据字符串节点值,还原Invoker
                        List<InvokerRegisterMessage> newInvokerList = Lists.newArrayList();
                        for (String each : currentChilds) {
                            newInvokerList.add(JacksonUtils.jsonToObject(each, InvokerRegisterMessage.class));
                        }

                        // 更新invoker缓存
                        invokerMap.put(nameSpace, newInvokerList);
                        logger.info("[{}]节点发生变化,重新加载该节点下的invoker信息如下", parentPath);
                        System.out.println(newInvokerList);
                    }
                });
            }
        }
        long times = System.currentTimeMillis() - startTime;
        logger.info("注册服务耗时{}ms [服务路径:/zookeeper/{}/{}]", times, nameSpace, provider.getServiceImplPath());
    }

    // 批量注册invoker,标签初始化是单个invoker注册,所以这个方法在本项目中暂时没用到
    @Override
    public void initInvokers(List<InvokerRegisterMessage> invokerRegisterMessages) {
        long startTime = System.currentTimeMillis();
        //连接zk
        synchronized (RegisterCenter.class) {
            for (InvokerRegisterMessage invoker : invokerRegisterMessages) {
                registerInvoker(invoker);
            }
        }
        long times = System.currentTimeMillis() - startTime;
        logger.info("从zookeeper中批量获取接口服务地址成功,耗时{}ms", times);
    }


    /**
     * 注册单个invoker
     */
    @Override
    public List<ProviderRegisterMessage> registerInvoker(InvokerRegisterMessage invoker) {
        long startTime = System.currentTimeMillis();
        List<ProviderRegisterMessage> providerRegisterMessages = null;
        // 创建服务接口的命名空间
        String nameSpace = invoker.getAppName() + "/" + invoker.getServicePath();
        synchronized (RegisterCenter.class) {
            // 创建invoker命名空间(持久节点)
            String invokerPath = ROOT_PATH + "/" + nameSpace + "/" + INVOKER_TYPE;
            boolean exist = zkClient.exists(invokerPath);
            if (!exist) {
                zkClient.createPersistent(invokerPath, true);
            }
            // 创建invoker注册信息节点(临时节点)
            String invokerMsgNode = invokerPath + "/" + JacksonUtils.objectToJson(invoker);
            // 创建临时节点(临时节点才能自动监听)
            exist = zkClient.exists(invokerMsgNode);
            if (!exist) {
                zkClient.createEphemeral(invokerMsgNode);
            }
            // 获取服务节点
            String servicePath = ROOT_PATH + "/" + nameSpace + "/" + PROVIDER_TYPE;
            // 本地缓存没有这个接口key,表明该接口是第一次添加引用声明,此时需要为该接口添加一个监听器
            // 不过接口引用声明一般也只会出现一次
            if (null == providerMap.get(nameSpace)) {
                // 为每个服务注册监听器,实现服务自动发现
                zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {
                    @Override
                    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                        // 服务接口的全限定名
                        if (null == currentChilds || currentChilds.size() == 0) {
                            providerMap.remove(nameSpace);
                            logger.warn("[{}]节点发生变化,该节点下已无可用服务", parentPath);
                            return;
                        }
                        // 监听到变化后servicePath节点下的所有临时节点值是currentChilds
                        List<ProviderRegisterMessage> newProviderList = Lists.newArrayList();
                        for (String each : currentChilds) {
                            newProviderList.add(JacksonUtils.jsonToObject(each, ProviderRegisterMessage.class));
                        }
                        // 更新本地缓存的服务信息
                        providerMap.put(nameSpace, newProviderList);
                        logger.info("[{}]节点发生变化,重新加载该节点下的服务信息如下", parentPath);
                        System.out.println(newProviderList);
                    }
                });
            }
            // 获取服务节点下所有临时节点(服务注册信息列表)
            List<String> providerStrings = zkClient.getChildren(servicePath);
            // 根据注册信息字符串还原注册信息
            providerRegisterMessages = Lists.newArrayList();
            for (String each : providerStrings) {
                providerRegisterMessages.add(JacksonUtils.jsonToObject(each, ProviderRegisterMessage.class));
            }
            // 将注册信息缓存到本地
            providerMap.put(nameSpace, providerRegisterMessages);
        }
        long times = System.currentTimeMillis() - startTime;
        logger.info("获取服务地址耗时{}ms:[{}]", times, nameSpace);
        return providerRegisterMessages;
    }

    @Override
    public Map<String, List<ProviderRegisterMessage>> getProviderMap() {
        return providerMap;
    }

    @Override
    public Map<String, List<InvokerRegisterMessage>> getInvokersOfProvider() {
        return invokerMap;
    }

    @Override
    public Map<String, List<String>> getProvidersOfInvoker() {
        Map<String, List<String>> map = Maps.newConcurrentMap();
        // 获取所有的服务接口节点
        List<String> apps = zkClient.getChildren(ROOT_PATH);
        for (String eachApp : apps) {
            logger.info("正在遍历的应用名称是:[{}]", eachApp);
            List<String> services = zkClient.getChildren(ROOT_PATH + "/" + eachApp);
            for (String eachService : services) {
                logger.info("正在遍历的服务名称是:[{}]", eachService);
                String nameSpace = eachApp + "/" + eachService;
                List<String> invokers = zkClient.getChildren(ROOT_PATH + "/" + nameSpace + "/" + INVOKER_TYPE);
                for (String eachInvoker : invokers) {
                    logger.info("遍历调用者一次");
                    String machineID4Server = JacksonUtils.jsonToObject(eachInvoker, InvokerRegisterMessage.class).getInvokerMachineID4Server();
                    List<String> list = map.get(machineID4Server);
                    if (null == list) {
                        list = new ArrayList<>();
                        map.put(machineID4Server, list);
                    }
                    list.add(nameSpace);
                }
            }
        }
        return map;
    }
}
