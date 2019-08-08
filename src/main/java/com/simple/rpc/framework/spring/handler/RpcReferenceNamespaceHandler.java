package com.simple.rpc.framework.spring.handler;

import com.simple.rpc.framework.spring.parser.RpcReferenceBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 服务引入自定义标签
 *
 * @author 11102342 suchang 2019/07/03
 */
public class RpcReferenceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("reference", new RpcReferenceBeanDefinitionParser());
    }
}
