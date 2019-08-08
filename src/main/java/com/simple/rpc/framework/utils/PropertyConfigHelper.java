package com.simple.rpc.framework.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * 配置导入类
 *
 * @author 11102342 suchang 2019/07/04
 */
public class PropertyConfigHelper {

    private static final Logger logger = LoggerFactory.getLogger(PropertyConfigHelper.class);

    private static final String PROPERTY_CLASSPATH = "/simple-rpc.properties";
    private static final Properties properties = new Properties();
    /*必须要显示声明的配置项(没有默认值)*/
    // ZK服务地址
    private static String zkService = "";
    /*标签优先级更高,如果标签声明了,则不需要再显示声明,标签没声明的使用配置项*/
    // 服务方注册时候的应用名
    private static String appName4Server = "";
    // 使用方引用时候的应用名
    private static String appName4Client = "";

    /*有默认值的配置项*/
    // ZK session超时时间
    private static int zkSessionTimeout;
    // ZK connection超时时间
    private static int zkConnectionTimeout;
    // 每个服务端提供者的Netty的连接数
    private static int channelPoolSize;
    // 客户端调用rpc服务线程池的线程数量
    private static int threadWorkerNumber;
    // 默认的负载均衡策略
    private static String defaultClusterStrategy;
    // 服务端采用的序列化协议
    private static String serverSerializer;
    // 客户端采用的序列化协议
    private static String clientSerializer;


    /**
     * 初始化
     */
    static {
        InputStream is = null;
        try {
            is = PropertyConfigHelper.class.getResourceAsStream(PROPERTY_CLASSPATH);
            if (null == is) {
                throw new IllegalStateException("ares_remoting.properties can not found in the classpath.");
            }
            properties.load(is);

            zkService = properties.getProperty("vivo.rpc.zookeeper.address");
            appName4Server = properties.getProperty("vivo.rpc.server.app.name");
            appName4Client = properties.getProperty("vivo.rpc.client.app.name");
            zkSessionTimeout = Integer.parseInt(properties.getProperty("vivo.rpc.zookeeper.session.timeout", "500"));
            zkConnectionTimeout = Integer.parseInt(properties.getProperty("vivo.rpc.zookeeper.connection.timeout", "500"));
            channelPoolSize = Integer.parseInt(properties.getProperty("vivo.rpc.client.channelPoolSize", "10"));
            threadWorkerNumber = Integer.parseInt(properties.getProperty("vivo.rpc.client.threadWorkers", "10"));
            defaultClusterStrategy = properties.getProperty("vivo.rpc.client.clusterStrategy.default", "random");
            serverSerializer = properties.getProperty("vivo.rpc.server.serializer", "Default");
            clientSerializer = properties.getProperty("vivo.rpc.client.serializer", "Default");

        } catch (Throwable t) {
            logger.warn("load ares_remoting's properties file failed.", t);
            throw new RuntimeException(t);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public static String getZkService() {
        return zkService;
    }

    public static int getZkSessionTimeout() {
        return zkSessionTimeout;
    }

    public static int getZkConnectionTimeout() {
        return zkConnectionTimeout;
    }

    public static int getChannelPoolSize() {
        return channelPoolSize;
    }

    public static int getThreadWorkerNumber() {
        return threadWorkerNumber;
    }

    public static String getDefaultClusterStrategy() {
        return defaultClusterStrategy;
    }

    public static String getAppName4Server() {
        return appName4Server;
    }

    public static String getAppName4Client() {
        return appName4Client;
    }

    public static String getServerSerializer() {
        return serverSerializer;
    }

    public static String getClientSerializer() {
        return clientSerializer;
    }
}
