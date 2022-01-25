package com.liu.nkcommunity.service;

import com.liu.nkcommunity.domain.Comment;

import java.util.List;

public interface CommentService {
    // 分页查询所有的评论信息
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    // 查询所有的评论数量
    int selectCountByEntity(int entityType, int entityId);
}
