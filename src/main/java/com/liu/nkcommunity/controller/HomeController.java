package com.liu.nkcommunity.controller;


import com.liu.nkcommunity.domain.DiscussPost;
import com.liu.nkcommunity.domain.Page;
import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.DiscussPostService;
import com.liu.nkcommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping("index")
    public String getIndexPage(Model model, Page page){
        // 获取总记录数
        page.setRows(discussPostService.selectDiscussPostRows(0));
        // 设置访问路径
        page.setPath("/index");

        // 查询所有的讨论贴
        List<DiscussPost> discussPostList = discussPostService.selectDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        // 将讨论贴设置在请求域中
        if (discussPostList != null){
            for (DiscussPost discussPost : discussPostList) {
                HashMap<String, Object> map = new HashMap<>();
                User user = userService.selectById(Integer.valueOf(discussPost.getUserId()));
                map.put("post", discussPost);
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "index";
    }



}
