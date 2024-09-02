package com.atguigu.gulimall.order;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = GulimallOrderApplication.class)
class GulimallOrderApplicationTests {
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//    @Test
//    void contextLoads() {
//        // 发送测试消息到队列
//        rabbitTemplate.convertAndSend("order.release.order.queue", "Test message");
//    }
//    @RabbitListener(queues = "order.release.order.queue")
//    public void handleOrderReleaseMessage(String message) {
//        // 打印接收到的消息
//        System.out.println("Received message from order.release.order.queue: " + message);
//        // 你可以在这里添加处理逻辑
//    }

}
