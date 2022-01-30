package com.liu.nkcommunity.service.impl;

import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.FollowService;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityConstant;
import com.liu.nkcommunity.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 实现关注和取关的功能
 */
@Service
public class FollowServiceImpl implements FollowService, CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

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

    // 查询某个用户关注的人（实体类型）
    @Override
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        // 获取当前用户关注的实体类型（这里查询的是人）的key
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        // 在有序集合中查询当前的key的值value的key（也就是当前用户关注的所有的人的userId）
        // 倒序排序 查询offset -> offset + limit - 1  表示的是当前页的数据信息
        // zset中存储的数据形式是  key: (entityId(userId), time)
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds == null){
            return null;
        }
        // 封装查询到的用户信息和关注的时间
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            HashMap<String, Object> map = new HashMap<>();
            User user = userService.selectById(targetId);
            map.put("user", user);
            // 获取 key: (userId, time) 中的time
            Double time = redisTemplate.opsForZSet().score(followeeKey, targetId);
            // 转换时间格式
            map.put("followTime", new Date(time.longValue()));
            list.add(map);
        }
        return list;
    }

    // 查询某个用户的粉丝（实体类型）
    @Override
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        // 获取当前用户粉丝的实体类型（这里查询的是人）的key(表示的是某个实体(用户)拥有的粉丝数量)
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        // 在有序集合中查询当前的key的值value的key（也就是当前用户的所有粉丝的userId）
        // 倒序排序 查询offset -> offset + limit - 1  表示的是当前页的数据信息
        // zset中存储的数据形式是  key: (userId, time)
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds == null){
            return null;
        }
        // 封装查询到的用户信息和关注的时间
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            HashMap<String, Object> map = new HashMap<>();
            User user = userService.selectById(targetId);
            map.put("user", user);
            // 获取 key: (userId, time) 中的time
            Double time = redisTemplate.opsForZSet().score(followerKey, targetId);
            // 转换时间格式
            map.put("followTime", new Date(time.longValue()));
            list.add(map);
        }
        return list;
    }


}


















