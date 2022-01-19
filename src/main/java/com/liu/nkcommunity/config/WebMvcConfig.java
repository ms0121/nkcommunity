package com.liu.nkcommunity.config;

import com.liu.nkcommunity.interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 表示使用那个拦截类去拦截请求信息，可以配置多个
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    /**
     * @param registry 用于配置拦截器和设置拦截的路径
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                // static下的所有静态资源都不进行拦截
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
