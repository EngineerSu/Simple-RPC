package com.simple.rpc.framework.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 服务端启动
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class MainServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainServer.class);

    public static void main(String[] args) throws Exception {
        long time = System.currentTimeMillis();

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("rpc-service.xml");

        time = System.currentTimeMillis() - time;
        LOGGER.warn("服务端启动成功,耗时{}ms", time);
    }

}
