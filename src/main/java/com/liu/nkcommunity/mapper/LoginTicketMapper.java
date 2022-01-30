package com.liu.nkcommunity.mapper;

import com.liu.nkcommunity.domain.LoginTicket;

// 不推荐使用，登录的凭证信息不再存储到数据库中
@Deprecated
public interface LoginTicketMapper {

    int insertLoginTicket(LoginTicket loginTicket);

    int updateLoginTicket(String ticket, int status);

    LoginTicket selectByTicket(String ticket);



}
