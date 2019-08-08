package com.simple.rpc.framework.test;

import com.simple.rpc.framework.test.service.MultiService;
import com.simple.rpc.framework.test.service.SayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 客户端启动并测试
 *
 * @author jacksu
 * @date 2018/8/8
 */
public class MainClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainClient.class);

    private static SayService sayService;
    private static MultiService multiService;

    // 客户端初始化
    static {
        long time = System.currentTimeMillis();
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("rpc-reference.xml");
        sayService = (SayService) context.getBean("sayService");
        multiService = (MultiService) context.getBean("multiService");
        time = System.currentTimeMillis() - time;
        LOGGER.info("客户端初始化完成,耗时{}ms", time);
    }

    /**
     * 提供了负载均衡和引用多服务的测试方法
     */
    public static void main(String[] args) throws InterruptedException {
        // 测试
        testLoadBalance();
        testMultiService();
        multiService.sayHi();
        // 客户端退出
        System.exit(0);
    }


    /**
     * 测试负载均衡:sayService的实现类有两个,发布服务时使用不同端口,所以相当于有两个服务地址
     * 它们的权重分别是50和100,调用足够多次服务,查看是否和负载均衡的预期结果一致
     */
    private static void testLoadBalance() {
        String name = "Simple-RPC";
        String returnValueOfService1 = String.format("[SayService1] Hi,%s. Are you OK?", name);
        String returnValueOfService2 = String.format("[SayService2] Hi,%s. Are you OK?", name);
        // 测试次数
        int times = 1000;
        // 记录服务被调用的次数
        int executeOfService1 = 0;
        int executeOfService2 = 0;
        while (times-- > 0) {
            String returnValue = sayService.sayHi(name);
            System.out.println(returnValue);
            if (returnValue.equals(returnValueOfService1)) {
                executeOfService1++;
            } else if (returnValue.equals(returnValueOfService2)) {
                executeOfService2++;
            }
        }
        LOGGER.info("权重为50的SayServiceImpl1被调用{}次,权重为100的SayServiceImpl2被调用{}次",
                executeOfService1, executeOfService2);
    }

    /**
     * 测试引用多服务功能正常
     */
    private static void testMultiService() throws InterruptedException {
        String name = "Simple-RPC";
        String returnValue = multiService.sayHi(name);
        System.out.println(returnValue);
    }


}
