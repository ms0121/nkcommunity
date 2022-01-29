package com.liu.nkcommunity.service;

// 实现关注和取关的功能
public interface FollowService {

    void follow(int userId, int entityType, int entityId);

    void unfollow(int userId, int entityType, int entityId);

    // 查询用户关注的实体数量
    long findFolloweeCount(int userId, int entityType);

    // 查询某个实体的粉丝数量
    long findFollowerCount(int entityType, int entityId);

    // 判断当前的用户是否已经关注了该实体
    boolean hasFollowed(int userId, int entityType, int entityId);
}
