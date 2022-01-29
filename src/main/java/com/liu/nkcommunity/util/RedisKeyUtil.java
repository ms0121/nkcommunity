package com.liu.nkcommunity.util;

public class RedisKeyUtil {
    // 分隔符
    private static final String SPLIT = ":";
    // 存放在redis中的key的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    // 记录某个用户的赞
    private static final String PREFIX_USER_LIKE = "like:user";
    // 关注的目标（被关注的用户的key）
    private static final String PREFIX_FOLLOWEE = "followee";
    // 粉丝的key(便于统计用户的粉丝数量)
    private static final String PREFIX_FOLLOWER = "follower";


    /**
     * 某个实体的赞的key
     * key     ==>     like:entity:entityType:entityId
     * value   ==>     userId(value存放的是用户id，这样子可以通过userId快速查询对应的用户信息)
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 记录某个用户的赞
    // 形式：like:user:userId  ---> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体key
    // key：表示某个用户关注的实体的类型(体现用户和实体之间的关系)
    // value：存放的是实体的id ，关注的时间
    // followee:userId:entityType  ---> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }


    // 某个实体拥有的粉丝key
    // key：表示某个类型的实体
    // value：存放的是用户id，时间
    // follower:entityType:entityId  ---> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

}
