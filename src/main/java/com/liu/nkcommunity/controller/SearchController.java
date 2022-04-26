package com.liu.nkcommunity.controller;

import com.liu.nkcommunity.domain.DiscussPost;
import com.liu.nkcommunity.domain.Page;
import com.liu.nkcommunity.service.LikeService;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.service.impl.ElasticSearchServiceImpl;
import com.liu.nkcommunity.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: lms
 * @date: 2022-04-26 23:08
 */
@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticSearchServiceImpl elasticSearchServiceImpl;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping(value = "/search")
    public String search(String keyword, Page page, Model model) {
        Map<String, Object> resMap =
                elasticSearchServiceImpl.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        // 总记录数目
        long total = (long) resMap.get("total");
        // 总记录内容
        List<DiscussPost> discussPostList = (List<DiscussPost>) resMap.get("discussPostList");
        // 聚合数据信息
        ArrayList<Map<String, Object>> discussPosts = new ArrayList<>();
        if (discussPostList != null) {
            for (DiscussPost discussPost : discussPostList) {
                HashMap<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", discussPost);
                // 作者
                map.put("user", userService.selectById(discussPost.getUserId()));
                // 点赞的数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);
        // 设置分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows((int) total);
        return "/site/search";
    }

}
