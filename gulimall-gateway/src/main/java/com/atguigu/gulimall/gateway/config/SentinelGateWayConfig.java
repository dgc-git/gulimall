package com.atguigu.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author dgc
 * @date 2024/9/9 14:44
 */
@Configuration
public class SentinelGateWayConfig {
    public SentinelGateWayConfig(){
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            //网关限流了，就会调用此回调Mono,Flux
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                R error = R.error(BizCodeEnum.TO0_MANY_REQUEST.getCode(), BizCodeEnum.TO0_MANY_REQUEST.getMessage());
                String s = JSON.toJSONString(error);
                Mono<ServerResponse> body = ServerResponse.ok().body(Mono.just(error), String.class);
                return body;
            }
        });
    }
}
