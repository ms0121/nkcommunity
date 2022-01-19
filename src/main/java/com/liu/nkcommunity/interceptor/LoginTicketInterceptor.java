package com.liu.nkcommunity.interceptor;

import com.liu.nkcommunity.domain.LoginTicket;
import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CookieUtil;
import com.liu.nkcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 1. 实现每次请求的拦截：目的就是判断当前的用户是否已经登录
 * 2. 进行配置拦截器以及要拦截的路径
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    // 请求执行前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取ticket凭证信息
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            // 根据ticket查询登录信息
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 判断当前登录信息凭证是否失效
            // after表示过期时间在当前时间的后面
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                User user = userService.selectById(loginTicket.getUserId());
                // 在本次请求中持有用户信息，只有当这次的请求结束之后，才会清掉该值
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    // 请求执行之后执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 获取当前线程的用户信息
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
