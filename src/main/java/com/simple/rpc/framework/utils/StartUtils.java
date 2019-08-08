package com.simple.rpc.framework.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动初始化类
 *
 * @author 11102342 suchang 2019/07/03
 */
public class StartUtils {

    private static final Logger logger = LoggerFactory.getLogger(StartUtils.class);

    public static void loadClasses() {
        // 加载预用类
        long time = System.currentTimeMillis();
        try {
            Class.forName("com.simple.rpc.framework.utils.JacksonUtils");
            Class.forName("com.simple.rpc.framework.utils.PropertyConfigHelper");
            Class.forName("com.simple.rpc.framework.invoker.ClientProxyBeanFactory");
            Class.forName("com.simple.rpc.framework.serialize.common.SerializerEngine");
            Class.forName("com.simple.rpc.framework.serialize.common.SerializerType");
            Class.forName("com.simple.rpc.framework.serialize.serializer.DefaultSerializer");
            Class.forName("com.simple.rpc.framework.serialize.serializer.HessianSerializer");
            Class.forName("com.simple.rpc.framework.serialize.serializer.ProtoStuffSerializer");
            Class.forName("com.simple.rpc.framework.serialize.handler.NettyDecoderHandler");
            Class.forName("com.simple.rpc.framework.serialize.handler.NettyEncoderHandler");

        } catch (ClassNotFoundException e) {
            logger.error("加载预用类出错:找不到类");
            e.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        logger.info("常用类预加载耗时{}ms", time);
    }
}
