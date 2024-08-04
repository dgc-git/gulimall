package com.atguigu.gulimall.coupon;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1.引入配置中心依赖
 * 2.创建一个bootstrap.properties,并告知配置中心地址以及本微服务的名称
 * 3.配置中心中添加一个当前应用名.properties
 * 4.添加任何配置到nacos的properties中
 * 5.动态获取配置：@refreshScope   @Value
 * 如果同时在本地application.properties中以及nacos中配置了相同参数，默认使用nacos中的配置
 */
@SpringBootApplication
@MapperScan("com.atguigu.gulimall.coupon.dao")
@EnableDiscoveryClient
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
