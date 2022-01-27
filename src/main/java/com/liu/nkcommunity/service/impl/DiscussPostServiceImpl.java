package com.liu.nkcommunity.service.impl;

import com.liu.nkcommunity.domain.DiscussPost;
import com.liu.nkcommunity.mapper.DiscussPostMapper;
import com.liu.nkcommunity.service.DiscussPostService;
import com.liu.nkcommunity.util.SensitiveFilter;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    @Override
    public int selectDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    // 发表新的讨论贴
    @Override
    public int insertDiscussPost(DiscussPost discussPost) {
        if (discussPost == null){
            throw new RuntimeException("参数不能为空！");
        }
        // 对html进行转义
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 对内容和标题进行敏感词的过滤
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        // 添加数据信息
        return discussPostMapper.insertDiscussPost(discussPost);
    }

    // 查询对应的帖子详情
    @Override
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.findDiscussPostById(id);
    }

    // 更新帖子的评论数量
    @Override
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }
}
