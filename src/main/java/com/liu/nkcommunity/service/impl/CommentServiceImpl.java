package com.liu.nkcommunity.service.impl;

import com.liu.nkcommunity.domain.Comment;
import com.liu.nkcommunity.mapper.CommentMapper;
import com.liu.nkcommunity.service.CommentService;
import com.liu.nkcommunity.service.DiscussPostService;
import com.liu.nkcommunity.util.CommunityConstant;
import com.liu.nkcommunity.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService, CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    @Override
    public List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    @Override
    public int selectCountByEntity(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 添加评论
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @Override
    public int addComment(Comment comment) {
        // 对标题和内容进行敏感词的过滤
        if (comment == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 添加评论
        // 对内容进行html的变换和敏感词的过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 更新帖子的评论数量(表示当前评论作用于帖子)
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            // 针对帖子的评论， 该评论作用于哪个具体的帖子
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            // 更新指定帖子的评论数量
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }

    // 根据id查询评论信息
    @Override
    public Comment selectCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
