package com.liu.nkcommunity.mapper;

import com.liu.nkcommunity.domain.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 分页查询所有的评论信息
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    // 查询所有的评论数量
    int selectCountByEntity(int entityType, int entityId);

    // 添加评论信息
    int insertComment(Comment comment);

    // 根据id查询评论信息
    Comment selectCommentById(int id);

}
