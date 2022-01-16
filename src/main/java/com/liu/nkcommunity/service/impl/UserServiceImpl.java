package com.liu.nkcommunity.service.impl;

import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.mapper.UserMapper;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityConstant;
import com.liu.nkcommunity.util.CommunityUtil;
import com.liu.nkcommunity.util.MailClient;
import com.sun.org.apache.bcel.internal.generic.IUSHR;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 下面的所有方法必须进行参数的校验工作
 */
@Service
public class UserServiceImpl implements UserService, CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    // thymeleaf模板引擎
    @Autowired
    private TemplateEngine templateEngine;


    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    @Override
    public User selectById(int id) {
        return userMapper.selectById(id);
    }

    /**
     * 实现用户注册的功能:
     *      参数非空校验，用户名，密码，邮箱等
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮件不能为空！");
            return map;
        }
        // 校验账号以及邮箱
        User u1 = userMapper.selectByName(user.getUsername());
        if (u1 != null){
            map.put("usernameMsg", "用户名已存在！");
            return map;
        }
        User u2 = userMapper.selectByEmail(user.getEmail());
        if (u2 != null){
            map.put("emailMsg", "邮箱已被注册使用！");
            return map;
        }
        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        // 使用md5更新新的密码
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        // 创建激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        // 插入成功之后，user.id会自动被填充回去
        userMapper.insertUser(user);

        // 激活注册账号的邮件信息
        User u3 = userMapper.selectByName(user.getUsername());
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 激活的路径地址
        // http://localhost:8080/community/activation/用户id/激活码
        String url = domain + contextPath + "/activation/" + u3.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        // 使用templateEngine将html转为string类型
        String content = templateEngine.process("/mail/activation", context);
        // 发送邮件
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    // 判断是否激活成功
    @Override
    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            // 重复进行了激活
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode() == code){
            // 更新激活的状态
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }else {
            // 激活失败
            return ACTIVATION_FAILURE;
        }
    }


}