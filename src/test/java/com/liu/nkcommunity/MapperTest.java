package com.liu.nkcommunity;

import com.liu.nkcommunity.domain.LoginTicket;
import com.liu.nkcommunity.mapper.LoginTicketMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class MapperTest extends NkcommunityApplicationTests {

    @Autowired
    private LoginTicketMapper loginTicketMapper;

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
}
