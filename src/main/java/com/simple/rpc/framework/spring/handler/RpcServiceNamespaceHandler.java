package com.simple.rpc.framework.spring.handler;

import com.simple.rpc.framework.spring.parser.RpcServiceBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 服务引入自定义标签:声明simple:service标签解析类
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class RpcServiceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("service", new RpcServiceBeanDefinitionParser());
    }
}
