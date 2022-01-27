package com.liu.nkcommunity.controller;

import com.liu.nkcommunity.domain.Message;
import com.liu.nkcommunity.domain.Page;
import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.MessageService;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 私信(会话)列表
     * @param model 向页面传递数据信息
     * @param page 来源于前端页面
     * @return
     */
    @GetMapping("letter/list")
    public String list(Model model, Page page){
        User user = hostHolder.getUser();
        // 设置分页信息(以及每次点击发生的跳转逻辑)
        page.setLimit(5);
        page.setPath("letter/list");
        // 查询当前用户的所有会话信息
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        List<Message> conversationList =
                messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        // 用于封装每个会话需要显示给前端页面的信息
        ArrayList<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null){
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
        model.addAttribute("conversations",conversations);
        // 查询当前用户所有的未读消息
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        return "/site/letter";
    }


    /**
     * 具体某个会话的详细信息
     * @param conversationId 查询当前会话的详细会话信息
     * @param page
     * @param model
     * @return
     */
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model){
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表的所有消息
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        // 存放所有的私信和其他的数据信息
        ArrayList<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null){
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
        return "/site/letter-detail";
    }


    /**
     * 查询消息的私信的对立方
     * @param conversationId 直接通过会话id获取会话的对立目标用户
     * @return
     */
    public User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int ids_0 = Integer.parseInt(ids[0]);
        int ids_1 = Integer.parseInt(ids[1]);
        // 当前登录的用户id等于ids_0
        if (hostHolder.getUser().getId() == ids_0){
            return userService.selectById(ids_1);
        }else {
            return userService.selectById(ids_0);
        }
    }























}
