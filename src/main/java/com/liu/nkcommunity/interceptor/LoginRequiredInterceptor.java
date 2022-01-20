package com.liu.nkcommunity.interceptor;

import com.liu.nkcommunity.annotation.LoginAnnotation;
import com.liu.nkcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 必须进行配置拦截器
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 表示拦截的到是方法，才会执行判断
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取方法对象，并且获取指定方法对象上的注解
            Method method = handlerMethod.getMethod();
            LoginAnnotation loginAnnotation = method.getAnnotation(LoginAnnotation.class);
            if (loginAnnotation != null && hostHolder.getUser() == null){
                // 请求该方法时未登录，直接跳转到登录页面，然后禁止往下执行
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}



















