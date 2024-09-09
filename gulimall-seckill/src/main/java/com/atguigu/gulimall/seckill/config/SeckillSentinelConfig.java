package com.atguigu.gulimall.seckill.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dgc
 * @date 2024/9/9 11:03
 */
@Configuration
public class SeckillSentinelConfig implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        // 设置 HTTP 状态码，可以设置为 429 或其他合适的状态码
        httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        httpServletResponse.setContentType("application/json;charset=UTF-8");

        // 自定义响应体
        R error = R.error(BizCodeEnum.TO0_MANY_REQUEST.getCode(), BizCodeEnum.TO0_MANY_REQUEST.getMessage());
        String errorStr = JSON.toJSONString(error);

        // 向客户端返回限流响应
        httpServletResponse.getWriter().write(errorStr);
    }
}
