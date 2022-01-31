package com.liu.nkcommunity.controller;

import com.liu.nkcommunity.domain.Event;
import com.liu.nkcommunity.domain.Page;
import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.event.EventProducer;
import com.liu.nkcommunity.service.FollowService;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityConstant;
import com.liu.nkcommunity.util.CommunityUtil;
import com.liu.nkcommunity.util.HostHolder;
import org.aspectj.weaver.IUnwovenClassFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 关注、取消关注:
 * 需求：
 * - 开发关注 取消关注的功能
 * - 统计用户的关注数、粉丝数
 * 关键：
 * - 若A关注了B，则A是B的粉丝 B是A的目标
 * - 关注的目标可以是用户、帖子、题目等等，在实现的时候将这些目标抽象成为实体
 */

/**
 * 关注列表、粉丝列表：
 *  业务层：
 *      - 查询某个用户关注的人，支持分页显示
 *      - 查询某个用户的粉丝，支持分页
 *  表现层：
 *      - 处理”查询关注的人“、”查询粉丝“请求
 *      - 编写”查询关注的人“、”查询粉丝“模板
 */

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 关注
     *
     * @param entityType 目前关注的实体类型只有用户
     * @param entityId  用户实体的id
     * @return
     */
    @PostMapping("follow")
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        // 关注(目前只是实现对用户的关注，帖子，评论的关注未实现，后续再改)
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);// 因为当前关注的只能是人
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "已关注!");
    }

    /**
     * 取消关注
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @PostMapping("unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        // 关注
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注!");
    }


    /**
     * 查询当前userId用户关注的用户
     * @param userId 被查询用户的id
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.selectById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        // 设置分页的信息
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        // 查询当前用户所有关注的实体类型(人)的数量
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        // 查询当前的userId用户关注的所有的人的信息
        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        // 判断当前的登录者是否已经关注过里面的某些人
        if (userList != null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                // 判断当前的登录人是否关注过这个用户
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);
        return "site/followee";
    }

    /**
     * 查询当前userId用户的粉丝
     * @param userId 被查询用户的id
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.selectById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        // 设置分页的信息
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        // 查询当前用户所有的粉丝实体类型(人)的数量
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        // 查询当前的userId用户所有粉丝的信息
        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        // 判断当前的登录者是否已经关注过里面的某些人
        if (userList != null){
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                // 判断当前的登录人是否关注过这个用户
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);
        return "site/follower";
    }


    // 判断当前的登录人是否关注过这个用户
    public boolean hasFollowed(int userId){
        // 未登录
        if (hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }


















}
