package com.liu.nkcommunity.service;

import com.liu.nkcommunity.domain.DiscussPost;

import java.util.List;

public interface DiscussPostService {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    int selectDiscussPostRows(int userId);

    // 发表新的讨论贴
    int insertDiscussPost(DiscussPost discussPost);

    // 查询帖子的详情信息
    DiscussPost findDiscussPostById(int id);

    // 更新帖子的评论数量
    int updateCommentCount(int id, int commentCount);

    // 更新帖子分数
    int updateScore(int id, double score);
}
