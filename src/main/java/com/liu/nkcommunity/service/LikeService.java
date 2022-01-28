package com.liu.nkcommunity.service;

public interface LikeService {

    // 点赞
    void like(int userId, int entityType, int entityId);

    // 统计点赞的数量
    long findEntityLikeCount(int entityType, int entityId);

    // 查询某人对某个实体的点赞状态
    int findEntityLikeStatus(int userId, int entityType, int entityId);
}
