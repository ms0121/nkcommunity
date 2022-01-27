package com.liu.nkcommunity;

import com.liu.nkcommunity.domain.LoginTicket;
import com.liu.nkcommunity.domain.Message;
import com.liu.nkcommunity.mapper.LoginTicketMapper;
import com.liu.nkcommunity.mapper.MessageMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class MapperTest extends NkcommunityApplicationTests {

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void insert() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(111);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        // 10分钟内有效
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        int flag = loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println("flag = " + flag);
    }


    @Test
    public void update() {
        // 查询
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println("loginTicket = " + loginTicket);
        // 更新
        int loginTicket1 = loginTicketMapper.updateLoginTicket("abc", 1);
        System.out.println("loginTicket1 = " + loginTicket1);
    }


    @Test
    public void testMessage(){

        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message : messages) {
            System.out.println("message = " + message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println("count = " + count);

        List<Message> messageList = messageMapper.selectLetters("111_112", 0, 10);
        messageList.forEach(message -> {
            System.out.println("message = " + message);
        });

        int count1 = messageMapper.selectLetterCount("111_112");
        System.out.println("count1 = " + count1);

        int unreadCount = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println("unreadCount = " + unreadCount);
    }








}
