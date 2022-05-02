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
    // 存储登录验证码的key
    private static final String PREFIX_KAPTCHA = "kaptcha";
    // 存储登录凭证信息的key
    private static final String PREFIX_TICKET = "ticket";
    // 存储用户信息的key
    private static final String PREFIX_USER = "user";
    // uv：独立访客
    private static final String PREFIX_UV = "uv";
    // dau日活跃用户
    private static final String PREFIX_DAU = "dau";


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

    // 获取登录验证码的key，给每个登录的用户一个随机的字符串作为用户的登录凭证信息
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 构造登录凭证的key（登录凭证是在用户登录的部分使用）
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户信息key
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 指定日期的uv
    public static String getUV(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 指定范围内的uv
    public static String getUV(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 指定日期的dau
    public static String getDAU(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 指定范围内的dau
    public static String getDAU(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

}
