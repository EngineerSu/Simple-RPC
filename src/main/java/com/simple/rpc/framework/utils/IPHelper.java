package com.simple.rpc.framework.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * IP工具类
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class IPHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPHelper.class);

    private static String hostIp;

    static {
        String ip = null;
        Enumeration allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                List<InterfaceAddress> InterfaceAddress = netInterface.getInterfaceAddresses();
                for (InterfaceAddress add : InterfaceAddress) {
                    InetAddress inetAddress = add.getAddress();
                    if (inetAddress instanceof Inet4Address) {
                        if (StringUtils.equals(inetAddress.getHostAddress(), "127.0.0.1")) {
                            continue;
                        }
                        ip = inetAddress.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.error("获取本机Ip失败:异常信息:" + e.getMessage());
            throw new RuntimeException(e);
        }
        hostIp = ip;
    }

    /**
     * 获取本机Ip:通过获取系统所有的networkInterface网络接口,然后遍历每个网络下的InterfaceAddress组
     * 获得一个符合 InetAddress instanceof Inet4Address 条件的IpV4地址
     */
    public static String localIp() {
        return hostIp;
    }
}
