package com.atguigu.gulimall.order.interceptor;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gulimall.order.config.AliPayConfig;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayAsyncVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dgc
 * @date 2024/9/6 12:10
 */
@RestController//响应success数据给支付宝
public class OrderPayedListener {
    @Autowired
    AliPayConfig aliPayConfig;
    @Autowired
    OrderService orderService;
    @PostMapping("/payed/notify")
    public String handleAlipayed(PayAsyncVO vo,HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
//        Map<String, String[]> map = request.getParameterMap();
//        for (String key : map.keySet()) {
//            String value = request.getParameter(key);
//            System.out.println("参数名："+key+"参数值："+value);
//        }
//        System.out.println("支付宝通知到位了，数据："+map);
        //验签
        HashMap<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String>iter=requestParams.keySet().iterator();iter.hasNext();){
            String name=(String)iter.next();
            String[] values=(String[])requestParams.get(name);
            String valueStr="";
            for (int i=0;i<values.length;i++){
                valueStr=(i==values.length-1)?valueStr+values[i]
                        : valueStr+values[i]+",";
            }
            //乱码解决,这段代码在出现乱码时使用
//            valueStr=new String(valueStr.getBytes("ISO-8859-1"),"utf-8");
            params.put(name,valueStr);
        }
        boolean signVerified= AlipaySignature.rsaCheckV1(params,aliPayConfig.getAlipay_public_key(),aliPayConfig.getCharset(),aliPayConfig.getSign_type());
        if(signVerified){
            System.out.println("签名验证成功 ");
            return orderService.handleAlipayed(vo);
        }else {
            System.out.println("签名验证失败");
            return "error";
        }
    }

}
