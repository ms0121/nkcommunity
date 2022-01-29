package com.liu.nkcommunity.service.impl;

import com.liu.nkcommunity.service.FollowService;
import com.liu.nkcommunity.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 实现关注和取关的功能
 */
@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 关注
     *
     * @param userId     当前用户
     * @param entityType 关注的实体类型 (用户 / 帖子 / 其他)
     * @param entityId   关注的实体id
     */
    @Override
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 当前用户关注的实体key(当前用户关注的实体类型对应的key)
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                // 当前类型的实体拥有的粉丝key
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 开启事务操作
                operations.multi();
                // 使用有序集合实现：(当前的用户添加对该实体的关注)
                redisTemplate.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                // 该实体的粉丝数量相应的增加该用户
                redisTemplate.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }


    /**
     * 取消关注
     *
     * @param userId     当前用户
     * @param entityType 关注的实体类型 (用户 / 帖子 / 其他)
     * @param entityId   关注的实体id
     */
    @Override
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 当前用户关注的实体key(当前用户关注的实体类型对应的key)
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                // 当前类型的实体拥有的粉丝key
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 开启事务操作
                operations.multi();
                // 使用有序集合实现： 取消关注(当前的用户取消对该实体的关注)
                redisTemplate.opsForZSet().remove(followeeKey, entityId);
                // 该实体的粉丝数量相应的删除该用户
                redisTemplate.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }


    // 查询用户关注的实体数量
    @Override
    public long findFolloweeCount(int userId, int entityType){
        // 获取当前用户关注不同实体类型的key
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        // 获取当前用户关注的实体数量(查询指定key的value值)
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询某个实体的粉丝数量
    @Override
    public long findFollowerCount(int entityType, int entityId){
        // 获取当前类型的实体key
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        // 获取当前实体的粉丝数量(查询指定key的value值)
        return redisTemplate.opsForZSet().zCard(followerKey);
    }


    // 判断当前的用户是否已经关注了该实体
    @Override
    public boolean hasFollowed(int userId, int entityType, int entityId){
        // 根据传入的实体类型和实体id来判断
        // 获取当前用户和该实体类型的key
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        // 判断redis中是否存在该key对应的map类型的key值是否存在(key,(entityId, score))
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

}
