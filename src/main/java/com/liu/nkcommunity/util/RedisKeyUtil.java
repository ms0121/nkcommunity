package com.liu.nkcommunity.util;

public class RedisKeyUtil {
    // 分隔符
    private static final String SPLIT = ":";
    // 存放在redis中的key的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

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
}
