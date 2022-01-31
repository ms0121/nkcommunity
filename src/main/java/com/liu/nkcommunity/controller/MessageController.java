package com.liu.nkcommunity.controller;

import com.alibaba.fastjson.JSONObject;
import com.liu.nkcommunity.domain.Message;
import com.liu.nkcommunity.domain.Page;
import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.MessageService;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityConstant;
import com.liu.nkcommunity.util.CommunityUtil;
import com.liu.nkcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * 发送私信：
 *  - 采用异步的方式发送私信
 *  - 发送成功后刷新私信列表
 *
 * 设置已读：
 *  - 访问私信详情时：将显示的私信状态设置为已读状态
 */
@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 私信(会话)列表
     *
     * @param model 向页面传递数据信息
     * @param page  来源于前端页面
     * @return
     */
    @GetMapping("/letter/list")
    public String list(Model model, Page page) {
        User user = hostHolder.getUser();
        // 设置分页信息(以及每次点击发生的跳转逻辑)
        page.setLimit(5);
        page.setPath("/letter/list");
        // 查询当前用户的所有会话信息
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        List<Message> conversationList =
                messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        // 用于封装每个会话需要显示给前端页面的信息
        ArrayList<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            // 遍历每个会话信息
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId())); // 当前会话的总条数
                // 当前会话中未读的消息的数量
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                // 查询与当前用户对话的用户信息
                // 如果当前用户的id等于消息的发送者，说明与之对话的用户就是接收者，反之
                int target = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.selectById(target));
                // 将每条封装好的会话消息封加入到list集合中，然后在前端页面进行遍历
                conversations.add(map);
            }
        }
        // 将会话信息设置在model中
        model.addAttribute("conversations", conversations);
        // 查询当前用户所有的未读消息
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        // 所有未读通知的数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "/site/letter";
    }


    /**
     * 具体某个会话的详细信息
     *
     * @param conversationId 查询当前会话的详细会话信息
     * @param page
     * @param model
     * @return
     */
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表的所有消息
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        // 存放所有的私信和其他的数据信息
        ArrayList<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("letter", message);
                // 记录当前消息对应的用户信息
                map.put("fromUser", userService.selectById(message.getFromId()));
                letters.add(map);
            }
        }
        // 返回给前端的数据信息
        model.addAttribute("letters", letters);
        // 查询当前会话的目标用户target
        model.addAttribute("target", getLetterTarget(conversationId));
        // 将消息设置为已读状态
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()){
            // 修改消息状态
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }


    // 返回消息的id列表，将未读状态修改为已读
    private List<Integer> getLetterIds(List<Message> letterList) {
        ArrayList<Integer> ids = new ArrayList<>();
        if (letterList != null){
            for (Message message : letterList) {
                // 表示当前是接受消息的一方
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }


    /**
     * 查询消息的私信的对立方
     *
     * @param conversationId 直接通过会话id获取会话的对立目标用户
     * @return
     */
    public User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int ids_0 = Integer.parseInt(ids[0]);
        int ids_1 = Integer.parseInt(ids[1]);
        // 当前登录的用户id等于ids_0
        if (hostHolder.getUser().getId() == ids_0) {
            return userService.selectById(ids_1);
        } else {
            return userService.selectById(ids_0);
        }
    }


    /**
     * 发送消息
     *
     * @param toName
     * @param content
     * @return
     */
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName, String content) {
        // 通过用户名查询目标用户的id，从而进行拼接会话信息的会话id
        User target = userService.findByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在!");
        }
        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < target.getId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        // 添加消息
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }


    /**
     * 显示系统通知
     *  通知列表
     *      - 显示评论、点赞、关注三种类型的通知
     *  通知详情
     *      - 分页显示某一类主题所包含的通知
     *  未读消息
     *      - 在页面头部显示所有的未读消息数量
     */
    @GetMapping("notice/list")
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        // 查询评论类通知
        // 最新的通知需要显示在页面中
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVo = new HashMap<>();
        if (message != null){
            messageVo.put("message", message);
            // 将转义字符进行反转
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            // 获取封装在content中的信息
            messageVo.put("user", userService.selectById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            // 查询当前主题下的通知的数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("count", count);
            // 查询该主题下未读消息的数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("unread", unread);
            model.addAttribute("commentNotice", messageVo);
        }

        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVo = new HashMap<>();
        if (message != null){
            messageVo.put("message", message);
            // 将转义字符进行反转
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            // 获取封装在content中的信息
            messageVo.put("user", userService.selectById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            messageVo.put("postId", data.get("postId"));

            // 查询当前主题下的点赞通知的数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVo.put("count", count);
            // 查询该主题下未读消息的数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVo.put("unread", unread);
            model.addAttribute("likeNotice", messageVo);
        }

        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVo = new HashMap<>();
        if (message != null){
            messageVo.put("message", message);
            // 将转义字符进行反转
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            // 获取封装在content中的信息
            messageVo.put("user", userService.selectById((Integer) data.get("userId")));
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));

            // 查询当前主题下的点赞通知的数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("count", count);
            // 查询该主题下未读消息的数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("unread", unread);
            model.addAttribute("followNotice", messageVo);
        }

        // 查询所有未读私信消息的数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        // 所有未读通知的数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

}
