package com.liu.nkcommunity.controller;

import com.liu.nkcommunity.domain.Comment;
import com.liu.nkcommunity.domain.DiscussPost;
import com.liu.nkcommunity.domain.Event;
import com.liu.nkcommunity.event.EventProducer;
import com.liu.nkcommunity.service.CommentService;
import com.liu.nkcommunity.service.DiscussPostService;
import com.liu.nkcommunity.util.CommunityConstant;
import com.liu.nkcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Service
@RequestMapping("comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;


    @PostMapping("add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        // 添加评论
        commentService.addComment(comment);

        // 触发评论的事件信息
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        // 主要就是获取帖子或者评论的作者
        // 因为当前的评论可能是给帖子又或者是给评论的
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 如果是给帖子做评论(表示评论的目标)
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            // 设置帖子的作者
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            // 表示当前给评论做评论
            Comment target = commentService.selectCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        // 发送事件
        eventProducer.fireEvent(event);

        // 如果当前的评论是作用在帖子上，则触发事件
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 触发事件：将发布分帖子添加到es服务器上
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityId(discussPostId)
                    .setEntityType(ENTITY_TYPE_POST);
            // 将贴子发布到kafka服务器上
            eventProducer.fireEvent(event);
        }

        // 跳转到帖子的详情页面
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
