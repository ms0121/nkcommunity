package com.liu.nkcommunity.controller;

import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @GetMapping("register")
    public String getRegisterPage(){
        return "site/register";
    }


    @GetMapping("login")
    public String getLoginPage(){
        return "site/login";
    }


    @PostMapping("register")
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()){
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            // 跳转到操作成功的页面
            return "site/operate-result";
        }else {
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
            @PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS){
            // 激活成功，并跳转到登录页面
            model.addAttribute("msg", "激活成功，您的账号可以正常使用了！");
            model.addAttribute("target", "/login");
        }else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作，该账号已被激活过！");
            model.addAttribute("target", "/index");
        }else {
            model.addAttribute("msg", "激活失败，激活码无效！");
            model.addAttribute("target", "/index");
        }
        // 返回中间跳转页面
        return "site/register";
    }



}
