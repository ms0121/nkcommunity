package com.liu.nkcommunity.controller;

import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.LikeService;
import com.liu.nkcommunity.util.CommunityUtil;
import com.liu.nkcommunity.util.HostHolder;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 点赞
     * @param entityType
     * @param entityId
     * @return
     */
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId){
        User user = hostHolder.getUser();
        // 点赞
        likeService.like(user.getId(), entityType, entityId);
        // 获取点赞的数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 获取当前用户对于当前的帖子和评论点赞的状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回结果数据
        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);
        return CommunityUtil.getJSONString(0, null, map);
    }



}
