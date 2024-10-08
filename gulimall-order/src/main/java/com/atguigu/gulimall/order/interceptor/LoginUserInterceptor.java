package com.atguigu.gulimall.order.interceptor;

import com.alibaba.nacos.shaded.org.checkerframework.checker.units.qual.A;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //@GetMapping("order/order/status/{orderSn}")
        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/status/**", uri);
        boolean match1 = new AntPathMatcher().match("/payed/notify", uri);
        if(match || match1){
            //todo 添加对requesthost的判断，更加安全
            //request.getRemoteHost();
            return true;
        }
        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
//        return true;
        if (attribute != null) {
            loginUser.set(attribute);
            return true;
        } else {
            request.getSession().setAttribute("'msg", "请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
