package com.simple.rpc.framework.spring.handler;

import com.simple.rpc.framework.spring.parser.RpcReferenceBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 服务引入自定义标签:声明simple:reference标签解析类
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class RpcReferenceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("reference", new RpcReferenceBeanDefinitionParser());
    }
}
