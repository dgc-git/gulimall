package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PayWebController {
    @Autowired
    OrderService orderService;

    @GetMapping(value = "/payOrder",produces = MediaType.TEXT_HTML_VALUE)
    public String payOrder(@RequestParam("orderSn") String orderSn) {
//        PayVO payVO = new PayVO();
//        payVO.setBody();//订单备注
//        payVO.setOut_trade_no();//订单号
//        payVO.setSubject();//订单主题
//        payVO.setTotal_amount();
        PayVO payVO=orderService.getOrderPay(orderSn);
        return null;
    }
}
