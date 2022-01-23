package com.liu.nkcommunity.service;

import com.liu.nkcommunity.domain.DiscussPost;

import java.util.List;

public interface DiscussPostService {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    int selectDiscussPostRows(int userId);

    // 发表新的讨论贴
    int insertDiscussPost(DiscussPost discussPost);

    // 查询帖子的详情信息
    DiscussPost findDiscussPostById(int id);
}
