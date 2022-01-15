package com.liu.nkcommunity.service;

import com.liu.nkcommunity.domain.User;

import java.util.Map;

public interface UserService {

    User selectById(int id);

    Map<String, Object> register(User user);

    public int activation(int userId, String code);
}
