package com.liu.nkcommunity.controller;

import com.liu.nkcommunity.domain.*;
import com.liu.nkcommunity.event.EventProducer;
import com.liu.nkcommunity.service.CommentService;
import com.liu.nkcommunity.service.DiscussPostService;
import com.liu.nkcommunity.service.LikeService;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityConstant;
import com.liu.nkcommunity.util.CommunityUtil;
import com.liu.nkcommunity.util.HostHolder;
import com.liu.nkcommunity.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发布新的帖子
     *
     * @param title   帖子title
     * @param content 帖子内容
     * @return
     */
    @PostMapping("add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        // 获取当前的登录用户信息
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还没有登录呢");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.insertDiscussPost(discussPost);

        // 触发事件：将发布分帖子添加到es服务器上
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityId(discussPost.getId())
                .setEntityType(ENTITY_TYPE_POST);
        // 将贴子发布到kafka服务器上
        eventProducer.fireEvent(event);

        // 发布新帖子的时候，计算它的最新得分
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey,  discussPost.getId());

        return CommunityUtil.getJSONString(0, "发布成功!");
    }


    /**
     * 查询具体的帖子的详情信息
     *
     * @param discussId 帖子的id
     * @param model
     * @return
     */
    @GetMapping("detail/{discussId}")
    public String findDiscussPost(@PathVariable("discussId") int discussId, Model model, Page page) {
        // 查询帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussId);
        model.addAttribute("post", discussPost);

        // 查询作者
        User user = userService.selectById(discussPost.getUserId());
        model.addAttribute("user", user);

        // 查询帖子的点赞信息
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussId);
        model.addAttribute("likeCount", likeCount);
        // 查询当前的用户是否给帖子点赞(用户未登录直接返回0)
        int likeStatus =
                hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_POST, discussId);
        model.addAttribute("likeStatus", likeStatus);

        // 设置分页的信息
        page.setLimit(5);
        // 复用分页的请求路径信息
        page.setPath("/discuss/detail/" + discussId);
        // 查询当前帖子下所有的评论数量
        page.setRows(discussPost.getCommentCount());

        // 查询对应帖子下的评论信息
        // 评论：给帖子的评论
        // 回复：给评论的评论
        // 帖子的评论列表
        List<Comment> commentList =
                commentService.selectCommentsByEntity(ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());

        // 将显示的数据封装在list中(vo = view object)
        // 这是 评论vo列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        // 遍历当前帖子下所有的评论信息
        if (commentList != null) {
            // 将当前的每一条评论的信息，对应的用户以及该评论下的评论等信息查询进行封装在一个map中
            for (Comment comment : commentList) {
                // 一个map就是一个评论的vo数据
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 评论作者
                commentVo.put("user", userService.selectById(comment.getUserId()));

                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                //点赞状态,需要判断当前用户是否登录，没有登录无法点赞
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 当前评论的回复列表
                List<Comment> replyList =
                        commentService.selectCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复vo列表
                ArrayList<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    // 遍历评论的每一条回复
                    for (Comment reply : replyList) {
                        // 将评论中的每一条回复进行封装成map
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复内容
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.selectById(reply.getUserId()));

                        // 查询给回复的点赞信息
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        //点赞状态,需要判断当前用户是否登录，没有登录无法点赞
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        // 查询当前回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.selectById(reply.getTargetId());
                        replyVo.put("target", target);
                        replyVoList.add(replyVo);
                    }
                }
                // 将回复添加到当前的评论中
                commentVo.put("replys", replyVoList);
                // 回复的数量
                int replyCount = commentService.selectCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                commentVoList.add(commentVo);
            }
        }
        // 将所有的评论添加到model中
        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }
}


















