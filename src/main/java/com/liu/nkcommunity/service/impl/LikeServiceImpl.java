package com.liu.nkcommunity.service.impl;

import com.liu.nkcommunity.service.LikeService;
import com.liu.nkcommunity.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

// 点赞的相关功能
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 点赞
     * @param userId     点赞的用户id()
     * @param entityType 判断当前的点赞是作用于帖子还是回复的
     * @param entityId   具体作用的是哪个帖子之下
     * @param entityUserId   帖子作者的id，userId用户给作者的帖子进行点赞
     */
    @Override
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        //        // 根据实体类型和实体id创建响应的key
        //        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //        // 判断当前用户是否已经点过赞（如果是第二次点赞，则进行移除（也就是取消点赞），否则就是点赞）
        //        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        //        if (isMember) {
        //            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        //        } else {
        //            redisTemplate.opsForSet().add(entityLikeKey, userId);
        //        }
        // 执行事务的操作，因为涉及到多个添加数据的操作
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 根据实体类型和实体id创建响应的key
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                // 创建帖子作者的key
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                // 判断当前用户是否已经点过赞（如果是第二次点赞，则进行移除（也就是取消点赞），否则就是点赞）
                // 属于查询操作，不用放在事务之内（因为放在事务之内不会马上得到结果，redis的事务操作会将所有的操作放在队列当中）
                // 只有提交操作的时候才会所有都执行
                Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);

                // 开启事务
                operations.multi();
                // 如果当前用户已经给帖子点过赞了
                if (isMember){
                    // 当前用户取消了对帖子的点赞
                    operations.opsForSet().remove(entityLikeKey, userId);
                    // 那么帖子作者得到的点赞数量响应的要进行 减1 操作
                    operations.opsForValue().decrement(userLikeKey);
                }else{
                    // 当前用户对帖子进行了点赞
                    operations.opsForSet().add(entityLikeKey, userId);
                    // 那么帖子作者得到的点赞数量响应的要进行 加1 操作
                    operations.opsForValue().increment(userLikeKey);
                }
                // 提交事务的操作
                return operations.exec();
            }
        });


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

    // 查询某个用户获取的点赞数量
    @Override
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }


}
