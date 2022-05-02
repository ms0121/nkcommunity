package com.liu.nkcommunity.controller;


import com.liu.nkcommunity.domain.DiscussPost;
import com.liu.nkcommunity.domain.Page;
import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.DiscussPostService;
import com.liu.nkcommunity.service.LikeService;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 跳转到首页
     *
     * @param model
     * @param page
     * @return
     */
    @RequestMapping({"index", "/"})
    public String getIndexPage(Model model, Page page) {
        // 获取总记录数
        page.setRows(discussPostService.selectDiscussPostRows(0));
        // 设置访问路径
        page.setPath("/index");
        // 查询所有的讨论贴
        List<DiscussPost> discussPostList = discussPostService.selectDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        // 将讨论贴设置在请求域中
        if (discussPostList != null) {
            for (DiscussPost discussPost : discussPostList) {
                HashMap<String, Object> map = new HashMap<>();
                User user = userService.selectById(discussPost.getUserId());
                map.put("post", discussPost);
                map.put("user", user);

                // 统计点赞的数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "index";
    }

    /**
     * 跳转至500这个错误页面
     *
     * @return
     */
    @RequestMapping("/errorMsg")
    public String getErrorPage() {
        return "/error/500";
    }

    /**
     * 无访问的权限功能跳转的页面
     * @return
     */
    @GetMapping("/denied")
    public String denied() {
        return "/error/404";
    }


}
