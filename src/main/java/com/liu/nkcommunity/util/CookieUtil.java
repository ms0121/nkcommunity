package com.liu.nkcommunity.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    /**
     * 根据请求域获取cookie中指定名字的值
     * @param request
     * @param name
     * @return
     */
    public static String getValue(HttpServletRequest request, String name){
        if (request == null || name == null){
            throw new IllegalArgumentException("参数异常!");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null){
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)){
                    // 直接将当前的cookie值传回
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
