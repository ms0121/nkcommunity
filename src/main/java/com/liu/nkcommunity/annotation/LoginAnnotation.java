package com.liu.nkcommunity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被使用该注解的方法，表示必须登录以后才能访问
 * 表示该注解只能作用在方法上，并且该注解只能在运行时有效
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginAnnotation {

}
