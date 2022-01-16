package com.liu.nkcommunity.controller;

import com.google.code.kaptcha.Producer;
import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @GetMapping("register")
    public String getRegisterPage() {
        return "site/register";
    }


    // 跳转登录页面
    @GetMapping("login")
    public String getLoginPage() {
        return "site/login";
    }

    // 生成验证码的接口
    @GetMapping("kaptcha")
    public void generateImage(HttpServletResponse response, HttpSession session) {
        // 生成随机数，并生成图片（保存到session中）
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        // 将验证码存放到session中
        session.setAttribute("kaptcha", text);

        // 设置响应给浏览器的类型为图片
        response.setContentType("image/png");

        // 因为输出的是图片，所以使用字节输出流比较好
        try {
            OutputStream os = response.getOutputStream();
            // 输出到页面
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            LOGGER.error("验证码生成失败: ", e.getMessage());
        }
    }


    // 注册页面
    @PostMapping("register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            // 跳转到操作成功的页面
            return "site/operate-result";
        } else {
            // 将所有的信息返回给页面
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/register";
        }
    }


    // http://localhost:8080/community/activation/用户id/激活码
    @GetMapping("activation/{userId}/{code}")
    public String activation(
            Model model,
            @PathVariable("userId") int userId,
            @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            // 激活成功，并跳转到登录页面
            model.addAttribute("msg", "激活成功，您的账号可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已被激活过！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，激活码无效！");
            model.addAttribute("target", "/index");
        }
        // 返回中间跳转页面
        return "site/register";
    }


}
