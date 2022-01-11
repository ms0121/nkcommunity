package com.liu.nkcommunity.controller;

import ch.qos.logback.core.pattern.ConverterUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

@Controller
@RequestMapping("alpha")
public class AlphaController {

    @RequestMapping("hello")
    @ResponseBody
    public String hello() {
        return "Hello springBoot！！！";
    }

    // http的底层数据原理
    @RequestMapping("http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // 请求
        // 获取请求的方式
        System.out.println("request.getMethod() = " + request.getMethod());
        // 请求的路径
        System.out.println("request.getContextPath() = " + request.getContextPath());
        // 获取请求中的所有header信息
        // 属于key-value的结构类型
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            System.out.println("value = " + value);
        }
        // 获取请求中携带的code值
        String code = request.getParameter("code");
        System.out.println("code = " + code);

        // 相应
        // 设置响应的返回值类型（html页面的方式）
        response.setContentType("text/html;charset=utf-8");
        // 获取响应输出流进行输出信息
        try (
                // 默认会将打开的输出流进行关闭
                PrintWriter writer = response.getWriter();
        ) {
            // 将响应数据输出到页面中去
            writer.println("<h1>牛客网社区论坛</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 请求参数
    // stu?name=zhangsan&age=20
    @GetMapping("stu")
    @ResponseBody
    public void demo1(
            @RequestParam(value = "name", required = false, defaultValue = "zhangsan") String name,
            @RequestParam(value = "age", required = false, defaultValue = "20") Integer age){
        System.out.println("name = " + name);
        System.out.println("age = " + age);
    }

    // 路径参数的方式
    // stu2/30
    @GetMapping("stu2/{id}")
    @ResponseBody
    public void demo2(@PathVariable("id") Integer id){
        System.out.println("id = " + id);
    }


    // 处理post请求
    @PostMapping("login")
    @ResponseBody
    public String login(String name, String pwd){
        System.out.println("name = " + name);
        System.out.println("pwd = " + pwd);
        return "login success!";
    }


    // 处理html页面
    @GetMapping("logout")
    public ModelAndView logout(){
        ModelAndView model = new ModelAndView();
        model.addObject("name", "zhangsan");
        model.addObject("age", 30);
        // 设置跳转的视图
        model.setViewName("logout");
        return model;
    }

    // 处理html页面2
    @GetMapping("logout2")
    public String logout2(Model model){
        model.addAttribute("name", "zhangsan");
        model.addAttribute("age", 30);
        // 设置跳转的视图
        return "logout";
    }









}
