package com.liu.nkcommunity.controller;

import com.google.code.kaptcha.Producer;
import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityConstant;
import com.liu.nkcommunity.util.CommunityUtil;
import com.liu.nkcommunity.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 优化登录模块：
 * 使用redis存储验证码：
 * -验证码需要频繁的访问于刷新，的性能的要求比较高
 * -验证码不需要永久保存，通常在一段时间之后就会失效
 * -分布式部署时，存在session共享的问题
 * <p>
 * 使用redis存储登录凭证：
 * -处理每次请求时，都要查询用户的登录凭证，访问的频率非常高
 * <p>
 * 使用redis存储用户信息：
 * -处理每次请求的时候，都需要根据凭证去查询用户的相关信息，访问的频率非常的高
 */
@Controller
public class LoginController implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 注册页
     *
     * @return
     */
    @GetMapping("register")
    public String getRegisterPage() {
        return "site/register";
    }


    /**
     * 跳转至登录页面
     *
     * @return
     */
    @GetMapping("login")
    public String getLoginPage() {
        return "site/login";
    }

    /**
     * 生成验证码的接口
     *
     * @param response
     * @param session
     */
    @GetMapping("kaptcha")
    public void generateImage(HttpServletResponse response, HttpSession session) {
        // 生成随机数，并生成图片（保存到session中）
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 之前的做法：直接存放在session中
        // 将验证码存放到session中
        // session.setAttribute("kaptcha", text);

        // 现在将验证码存放在redis中
        // 获取标识当前用户的唯一凭证字符串，并将该字符串存放在cookie中
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 生成该用户的key
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        // 将验证码存储在redis中，并设置验证码的过期时间为60秒
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        try {
            // 设置响应给浏览器的类型为图片
            response.setContentType("image/png");
            // 因为输出的是图片，所以使用字节输出流比较好
            OutputStream os = response.getOutputStream();
            // 输出到页面
            ImageIO.write(image, "png", os);

        } catch (IOException e) {
            LOGGER.error("验证码生成失败: ", e.getMessage());
            LOGGER.error("生成的验证码无效，请重新尝试！");
        }
    }


    /**
     * 注册页面
     *
     * @param model
     * @param user
     * @return
     */
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

    /**
     * 激活新注册用户
     *
     * @param model
     * @param userId
     * @param code
     * @return
     */
    // http://localhost:8080/community/activation/用户id/激活码
    @GetMapping("activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
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

    /**
     * 登录方法
     *
     * @param username
     * @param password
     * @param code       验证码
     * @param rememberme 判断是否选中记住我功能
     * @param session    获取存在session中的验证码
     * @param model
     * @param response   设置cookie信息
     * @return
     */
    @PostMapping("login")
    public String login(String username, String password, String code, boolean rememberme,
                        HttpSession session, Model model, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 方法1：从session中获取
        // 从session中获取验证码
        // String kaptcha = (String) session.getAttribute("kaptcha");

        // 方法2：从cookie中获取用户的唯一凭证
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            // 生成该用户的key
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isBlank(code) || StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "site/login";
        }
        // 判断是否选中记住我的功能
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        // 验证信息
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        // 判断是否登录成功
        if (map.containsKey("ticket")) {
            // 设置cookie,作用的路径，有效时间
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            // 重定向到首页
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/login";
        }
    }

    /**
     * 直接从cookie里面获取指定的cookie值
     *
     * @param ticket
     * @return
     */
    @GetMapping("logout")
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
//        SecurityContextHolder.clearContext();
        // 退出后重定向到登陆页面
        return "redirect:/index";
    }


}
