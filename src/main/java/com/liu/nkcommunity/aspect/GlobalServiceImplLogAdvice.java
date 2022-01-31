package com.liu.nkcommunity.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 全局日记处理：将其封装成为一个切面，然后将其织入到目标方法中
 * 切面 = 通知 + 切入点
 */
@Component
@Aspect
public class GlobalServiceImplLogAdvice {

    // 记录日记信息
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalServiceImplLogAdvice.class);

    // 拦截任何impl中的所有类，所有方法，任何返回值和参数的方法
    // JoinPoint：连接点：可以理解为两个圆形的切点，从这个切点就可以获取到当前执行的目标类及方法
    @Before(value = "execution(* com.liu.nkcommunity.service.impl.*.*(..))")
    public void before(JoinPoint joinPoint) {
        // 日记打印形式：用户 [127.0.0.1], 在【某某时间】，访问了 【具体的方法】
        // 通过连接点获取当前请求的ip地址
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 处理消息队列出现的异常(因为消息事件是直接通过controller进行调用的，所以需要不处理)
        if (attributes == null){
            return;
        }
        // 获取用户请求
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // 通过连接点获取目标方法
        String typeName = joinPoint.getSignature().getDeclaringTypeName();  // 获取当前类
        String name = joinPoint.getSignature().getName(); // 方法名
        String target = typeName + "." + name;
        LOGGER.info(String.format("用户 [%s]，在[%s]，访问了[%s]", ip, now, target));
    }

}

