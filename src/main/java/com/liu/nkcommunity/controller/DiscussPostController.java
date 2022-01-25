package com.liu.nkcommunity.controller;

import com.liu.nkcommunity.domain.DiscussPost;
import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.DiscussPostService;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityUtil;
import com.liu.nkcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("discuss")
public class DiscussPostController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    /**
     * 发布新的帖子
     * @param title 帖子title
     * @param content 帖子内容
     * @return
     */
    @PostMapping("add")
    @ResponseBody
    public String addDiscussPost(String title, String content){
        // 获取当前的登录用户信息
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(403,"您还没有登录呢");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.insertDiscussPost(discussPost);
        return CommunityUtil.getJSONString(0, "发布成功!");
    }


    /**
     * 查询具体的帖子的详情信息
     * @param discussId 帖子的id
     * @param model
     * @return
     */
    @GetMapping("detail/{discussId}")
    public String findDiscussPost(@PathVariable("discussId") int discussId, Model model){
        // 查询帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussId);
        model.addAttribute("post", discussPost);
        // 查询用户的信息
        User user = userService.selectById(discussPost.getUserId());
        model.addAttribute("user", user);
        return "/site/discuss-detail";
    }




}
