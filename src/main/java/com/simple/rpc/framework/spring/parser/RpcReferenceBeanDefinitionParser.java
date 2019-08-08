package com.simple.rpc.framework.spring.parser;

import com.simple.rpc.framework.spring.factory.RpcReferenceFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import com.simple.rpc.framework.utils.PropertyConfigHelper;

/**
 * 服务引入自定义标签解析器:解析标签内容,生成一个getBeanClass返回类型的对象
 *
 * @author 11102342 suchang 2019/07/03
 */
public class RpcReferenceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final Logger logger = LoggerFactory.getLogger(RpcReferenceBeanDefinitionParser.class);

    protected Class getBeanClass(Element element) {
        return RpcReferenceFactoryBean.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder bean) {

        try {
            long startTime = System.currentTimeMillis();
            String id = element.getAttribute("id");
            String timeOut = element.getAttribute("timeout");
            String targetInterface = element.getAttribute("interface");
            String clusterStrategy = element.getAttribute("clusterStrategy");
            String groupName = element.getAttribute("groupName");
            String appName = element.getAttribute("appName");

            bean.addPropertyValue("timeout", Integer.parseInt(timeOut));
            bean.addPropertyValue("targetInterface", Class.forName(targetInterface));

            if (!StringUtils.isBlank(clusterStrategy)) {
                bean.addPropertyValue("clusterStrategy", clusterStrategy);
            }
            if (!StringUtils.isBlank(groupName)) {
                bean.addPropertyValue("groupName", groupName);
            }
            if (!StringUtils.isBlank(appName)) {
                bean.addPropertyValue("appName", appName);
            } else {
                String appName4Client = PropertyConfigHelper.getAppName4Client();
                if (StringUtils.isBlank(appName4Client)) {
                    logger.error("请配置{[}]标签的appName属性或在classpath:simple-rpc.properties中配置simple.rpc.client.app.name属性", id);
                    throw new Exception(id + "标签缺少appName属性");
                }
                bean.addPropertyValue("appName", appName4Client);
            }
            long times = System.currentTimeMillis() - startTime;
            logger.info("接口标签解析耗时{}ms:[{}]", times, targetInterface);
        } catch (Exception e) {
            logger.error("RevokerFactoryBeanDefinitionParser error.", e);
            throw new RuntimeException(e);
        }

    }
}
