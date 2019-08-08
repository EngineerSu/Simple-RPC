package com.simple.rpc.framework.zookeeper.message;

import java.io.Serializable;

/**
 * 服务注册在zk的信息,也是客户端获取的服务信息
 *
 * @author 11102342 suchang 2019/6/27
 */
public class ProviderRegisterMessage implements Serializable {

    // 接口所在应用名,解决接口重名冲突问题
    private String appName;
    // 接口的全限定名,作为这个服务的key
    private String servicePath;
    // 接口实现类的全限定名,是服务注册需要存储的信息之一
    private String serviceImplPath;
    // 提供服务的本机地址/端口
    private String serverIp;
    private int serverPort;
    // 服务端配置的超时时间
    private long timeout;
    // 服务端限流,默认为10
    private int workerThread;
    // 该服务提供者权重,默认为1,范围[1,100]
    private int weight;
    // 服务分组组名.默认为"default"
    private String groupName;

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getServiceImplPath() {
        return serviceImplPath;
    }

    public void setServiceImplPath(String serviceImplPath) {
        this.serviceImplPath = serviceImplPath;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(int workerThread) {
        this.workerThread = workerThread;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
