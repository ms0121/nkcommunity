package com.liu.nkcommunity.mapper;

import com.liu.nkcommunity.domain.LoginTicket;

public interface LoginTicketMapper {

    int insertLoginTicket(LoginTicket loginTicket);

    int updateLoginTicket(String ticket, int status);

    LoginTicket selectByTicket(String ticket);

}
