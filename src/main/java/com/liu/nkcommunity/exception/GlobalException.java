package com.liu.nkcommunity.exception;

import com.liu.nkcommunity.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// 全局异常统一处理
// 默认扫描所有的bean对象，这里只扫描Controller注解的下类
@ControllerAdvice
public class GlobalException {

    // 打印日记信息
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalException.class);

    /**
     * 处理所有的异常信息
     *
     * @param e        方法抛出的异常信息
     * @param request  获取发生异常信息方法的数据信息
     * @param response 做出响应数据的设置
     */
    @ExceptionHandler(value = Exception.class)
    public void handlerException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.error("服务器发生异常: " + e.getMessage());
        // 循环遍历打印错误信息
        for (StackTraceElement element : e.getStackTrace()) {
            LOGGER.error(element.toString());
        }

        // 获取出现异常的方法：发送请求的方式（如返回的时json数据还是跳转页面）
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            // 表示当前方法返回的是json数据
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常!"));
        } else {
            response.sendRedirect(request.getContextPath() + "/errorMsg");
        }
    }
}
