package com.liu.nkcommunity.service.impl;

import com.liu.nkcommunity.service.LikeService;
import com.liu.nkcommunity.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

// 点赞的相关功能
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 点赞
     * @param userId     统计点赞的用户id
     * @param entityType 判断当前的点赞是作用于帖子还是回复的
     * @param entityId   具体作用的是哪个帖子之下
     */
    @Override
    public void like(int userId, int entityType, int entityId) {
        // 根据实体类型和实体id创建响应的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 判断当前用户是否已经点过赞（如果是第二次点赞，则进行移除（也就是取消点赞），否则就是点赞）
        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (isMember) {
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
    }

    /**
     * 查询某个帖子或者回复的点赞数量信息
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        // 根据实体类型和实体id创建响应的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某人对某个实体的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        // 根据实体类型和实体id创建响应的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 判断当前用户是否已经点过赞（如果是第二次点赞，则进行移除（也就是取消点赞），否则就是点赞）
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
}
