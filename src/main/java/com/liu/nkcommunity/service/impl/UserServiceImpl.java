package com.liu.nkcommunity.service.impl;

import com.liu.nkcommunity.domain.LoginTicket;
import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.mapper.UserMapper;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityConstant;
import com.liu.nkcommunity.util.CommunityUtil;
import com.liu.nkcommunity.util.MailClient;
import com.liu.nkcommunity.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 下面的所有方法必须进行参数的校验工作
 * 使用redis重构用户登录之后的凭证信息
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

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询用户的接口十分的频繁，所以使用redis进行缓存用户的信息
     * 步骤：
     * - 1.需要查询用户信息的时候，优先从缓存中查询
     * - 2.缓存中可能没有用户信息，没有就从数据库中进行查询，并把查询得到的数据更新到redis中
     * - 3.如果修改了用户的相关信息，也就是用户的信息发生了变化，就直接从redis中将其进行删除（更新数据的方式可能会出现并发安全的问题）
     */
    @Override
    public User selectById(int id) {
        // return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    // 1.从缓存中查询数据
    private User getCache(int userId) {
        // 构造用户的key
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    // 2.取不到数据，先从数据库中获取，并初始化缓存中的用户信息
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        // 构造用户的key
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user);
        return user;
    }

    // 3.用户数据发生变化的时候，清除缓存数据信息
    public void clearCache(int userId) {
        // 构造用户的key
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }


    /**
     * 实现用户注册的功能:
     * 参数非空校验，用户名，密码，邮箱等
     *
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮件不能为空！");
            return map;
        }
        // 校验账号以及邮箱
        User u1 = userMapper.selectByName(user.getUsername());
        if (u1 != null) {
            map.put("usernameMsg", "用户名已存在！");
            return map;
        }
        User u2 = userMapper.selectByEmail(user.getEmail());
        if (u2 != null) {
            map.put("emailMsg", "邮箱已被注册使用！");
            return map;
        }
        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
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
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            // 重复进行了激活
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode() == code) {
            // 更新激活的状态
            userMapper.updateStatus(userId, 1);
            // 修改了用户信息，则删除缓存中的数据
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            // 激活失败
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 实现登录的逻辑判断
     *
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    @Override
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "用户名不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        // 判断用户是否存在
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "用户不存在!");
            return map;
        }
        // 对密码进行加密处理
        password = CommunityUtil.md5(password + user.getSalt());
        if (!password.equals(user.getPassword())) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }
        // 生成用户的凭证信息
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        // 方法1：之前的方法将用户登录之后的凭证信息保存到数据库
        // loginTicketMapper.insertLoginTicket(loginTicket);

        // 方法2：现在将用户登录之后生成的凭证保存到redis中
        // 获取凭证的key
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // 将凭证作为key,整个对象序列化成为json形式的数据保存到redis中
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 实现退出登录的操作
     *
     * @param ticket 根据用户ticket进行退出操作
     */
    @Override
    public void logout(String ticket) {
        // 修改凭证为无效
        // loginTicketMapper.updateLoginTicket(ticket, 1);

        // 从redis中取出对应的值，修改登录的状态
        // 获取凭证的key
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        // 覆盖原有的值
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    /**
     * 根据凭证ticket查询 LoginTicket 信息
     *
     * @param ticket
     * @return
     */
    @Override
    public LoginTicket findLoginTicket(String ticket) {
        // return loginTicketMapper.selectByTicket(ticket);
        // 获取凭证的key
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return loginTicket;
    }

    /**
     * 更新用户的头像url
     *
     * @param userId
     * @param headerUrl
     */
    @Override
    public void updateHeader(int userId, String headerUrl) {
        userMapper.updateHeader(userId, headerUrl);
        // 清除缓存
        clearCache(userId);
    }

    /**
     * 修改指定用户的密码
     *
     * @param id
     * @param password
     * @return
     */
    @Override
    public int updatePassword(int id, String password) {
        // 生成随机盐
        User user = userMapper.selectById(id);
        String pwd = CommunityUtil.md5(password + user.getSalt());
        int i = userMapper.updatePassword(user.getId(), pwd);
        // 清除缓存
        clearCache(user.getId());
        return i;
    }

    /**
     * 根据用户名查询用户信息
     *
     * @param name
     * @return
     */
    @Override
    public User findByName(String name) {
        return userMapper.selectByName(name);
    }

//    /**
//     * 查询用户的权限
//     *
//     * @param userId
//     * @return
//     */
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
//        User user = userMapper.selectById(userId);
//        List<GrantedAuthority> list = new ArrayList<>();
//        // 将用户的权限添加到列表中
//        list.add(new GrantedAuthority() {
//            @Override
//            public String getAuthority() {
//                switch (user.getType()) {
//                    case 1:
//                        return AUTHORITY_ADMIN;
//                    case 2:
//                        return AUTHORITY_MODERATOR;
//                    default:
//                        return AUTHORITY_USER;
//                }
//            }
//        });
//        return list;
//    }
}
