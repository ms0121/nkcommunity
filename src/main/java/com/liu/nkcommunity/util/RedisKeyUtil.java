package com.liu.nkcommunity.util;

import com.sun.org.glassfish.external.statistics.Statistic;

public class RedisKeyUtil {
    // 分隔符
    private static final String SPLIT = ":";
    // 存放在redis中的key的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    // 记录某个用户的赞
    private static final String PREFIX_USER_LIKE = "like:user";

    /**
     * 某个实体的赞的key
     * key     ==>     like:entity:entityType:entityId
     * value   ==>     userId(value存放的是用户id，这样子可以通过userId快速查询对应的用户信息)
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 记录某个用户的赞
    // 形式：like:user:userId  ---> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

}
