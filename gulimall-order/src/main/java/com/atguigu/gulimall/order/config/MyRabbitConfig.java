package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author dgc
 * @date 2024/8/29 12:15
 */
@Configuration
public class MyRabbitConfig {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @PostConstruct//这个类对象创建完之后调用
    public void initRabbitTemplate() {
        /**
         * 服务收到消息就回调
         * 1.配置文件配置
         * 2.设置确认回调
         */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                //服务器收到
                //todo 修改消息状态为服务器已收到（已发送->已收到）
                System.out.println(correlationData + "===>" + b + "===>" + s);
            }
        });
        //设置消息抵达队列回调,当消息没有成功投递到队列时，触发回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int i, String s, String s1, String s2) {
                //报错误了
                //需要修改数据库当前消息的状态->错误。（已收到->错误抵达）
                //todo 是否需要消费者消费消息后再修改消息记录呢->可以不用，因为使用幂等性可以解决这个问题，即使重复消费也不会出现问题
                System.out.println(message.toString()+i+s+s1+s2);
            }
        });
    }

}
