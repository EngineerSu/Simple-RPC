package com.simple.rpc.framework.spring.handler;

import com.simple.rpc.framework.spring.parser.RpcServiceBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 服务发布自定义标签
 *
 * @author 11102342 suchang 2019/07/03
 */
public class RpcServiceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("service", new RpcServiceBeanDefinitionParser());
    }
}
