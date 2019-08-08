package com.simple.rpc.framework.spring.parser;

import com.simple.rpc.framework.spring.factory.RpcServiceFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import com.simple.rpc.framework.utils.PropertyConfigHelper;

/**
 * 服务发布自定义标签解析器:解析标签内容,生成一个getBeanClass返回类型的对象
 *
 * @author 11102342 suchang 2019/07/03
 */
public class RpcServiceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    //logger
    private static final Logger logger = LoggerFactory.getLogger(RpcServiceBeanDefinitionParser.class);


    protected Class getBeanClass(Element element) {
        return RpcServiceFactoryBean.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        long startTime = System.currentTimeMillis();
        try {
            String id = element.getAttribute("id");
            String serviceItf = element.getAttribute("interface");
            String timeOut = element.getAttribute("timeout");
            String serverPort = element.getAttribute("serverPort");
            String ref = element.getAttribute("ref");
            String weight = element.getAttribute("weight");
            String workerThreads = element.getAttribute("workerThreads");
            String groupName = element.getAttribute("groupName");
            String appName = element.getAttribute("appName");

            bean.addPropertyValue("servicePath", serviceItf);
            // 作用是设置接口实现类全限定名
            bean.addPropertyReference("ref", ref);
            // 解析服务端口,不需要配置本机ip,直接通过程序获取
            bean.addPropertyValue("serverPort", Integer.parseInt(serverPort));
            bean.addPropertyValue("timeout", Integer.parseInt(timeOut));

            if (StringUtils.isNotBlank(groupName)) {
                bean.addPropertyValue("groupName", groupName);
            }
            if (NumberUtils.isNumber(weight)) {
                bean.addPropertyValue("weight", Integer.parseInt(weight));
            }
            if (NumberUtils.isNumber(workerThreads)) {
                bean.addPropertyValue("workerThreads", Integer.parseInt(workerThreads));
            }
            if (!StringUtils.isBlank(appName)) {
                bean.addPropertyValue("appName", appName);
            } else {
                String appName4Server = PropertyConfigHelper.getAppName4Server();
                if (StringUtils.isBlank(appName4Server)) {
                    logger.error("请配置{[}]标签的appName属性或在classpath:simple-rpc.properties中配置simple.rpc.server.app.name属性", id);
                    throw new Exception(id + "标签缺少appName属性");
                }
                bean.addPropertyValue("appName", appName4Server);
            }
            long times = System.currentTimeMillis() - startTime;
            logger.info("[{}]标签解析耗时{}ms", id, times);
        } catch (Exception e) {
            logger.error("ProviderFactoryBeanDefinitionParser error.", e);
            throw new RuntimeException(e);
        }

    }
}
