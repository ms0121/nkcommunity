package com.liu.nkcommunity.controller;

import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.FollowService;
import com.liu.nkcommunity.util.CommunityUtil;
import com.liu.nkcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 关注、取消关注:
 *  需求：
 *      - 开发关注 取消关注的功能
 *      - 统计用户的关注数、粉丝数
 *  关键：
 *      - 若A关注了B，则A是B的粉丝 B是A的目标
 *      - 关注的目标可以是用户、帖子、题目等等，在实现的时候将这些目标抽象成为实体
 */
@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 关注
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @PostMapping("follow")
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        // 关注(目前只是实现对用户的关注，帖子，评论的关注未实现，后续再改)
        followService.follow(user.getId(), entityType, entityId);
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
}
