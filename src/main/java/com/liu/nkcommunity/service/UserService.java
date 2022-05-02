package com.liu.nkcommunity.service;

import com.liu.nkcommunity.domain.LoginTicket;
import com.liu.nkcommunity.domain.User;

import java.util.Map;

public interface UserService {

    User selectById(int id);

    Map<String, Object> register(User user);

    int activation(int userId, String code);

    Map<String, Object> login(String username, String password, int expiredSeconds);

    void logout(String ticket);

    LoginTicket findLoginTicket(String ticket);

    void updateHeader(int userId, String headerUrl);

    int updatePassword(int id, String password);

    User findByName(String name);

}













