package com.atguigu.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: ThreadPoolConfigProperties
 * Package: com.atguigu.gulimall.product.config
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/25 -18:39
 * @Version: v1.0
 */
@ConfigurationProperties(prefix = "gulimall.thread")
@Component//放入容器中
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
