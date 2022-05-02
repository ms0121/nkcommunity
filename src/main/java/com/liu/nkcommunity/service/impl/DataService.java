package com.liu.nkcommunity.service.impl;

import com.liu.nkcommunity.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: lms
 * @date: 2022-05-02 12:45
 */
@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 格式化日期类
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");


    // 记录当前登陆用户的ip到当前的日期的key当中
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUV(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    // 统计指定日期期间内的uv总数
    public long caculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 需要将开始和结束日期之间的所有日期统计成为对应的keys，并将其转为数组传到redis中
        Calendar calendar = Calendar.getInstance();
        // 设置开始的时间
        calendar.setTime(start);
        List<String> dateList = new ArrayList<>();
        // 对时间进行遍历
        while (!calendar.getTime().after(end)) { // 当前时间在结束时间之前
            // 获取当前的时间对应的key
            String key = RedisKeyUtil.getUV(df.format(calendar.getTime()));
            dateList.add(key);
            // 时间加一天
            calendar.add(Calendar.DATE, 1);
        }
        // 创建start-end时间的key
        String redisKey = RedisKeyUtil.getUV(df.format(start), df.format(end));
        // 将指定日期之间的所有的数据进行合并到redisKey中
        redisTemplate.opsForHyperLogLog().union(redisKey, dateList.toArray());
        // 返回指定期间的总数
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    // 将指定的用户统计到dau
    public void recordDAU(int userId) {
        // 获取指定日期的key
        String redisKey = RedisKeyUtil.getDAU(df.format(new Date()));
        // 将该用户的位设置为true，表示当前的用户于当天已经登陆过
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    // 统计指定时间段的dau数量，直接使用或运算
    public long caculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 使用calendar来进行遍历日期
        Calendar calendar = Calendar.getInstance();
        // 设置开始遍历的位置
        calendar.setTime(start);
        List<byte[]> dateList = new ArrayList<>();
        // 当前的时间点在结束之前
        while (!calendar.getTime().after(end)) {
            // 获取对应的key
            String key = RedisKeyUtil.getDAU(df.format(calendar.getTime()));
            // 转成为字节数组
            dateList.add(key.getBytes(StandardCharsets.UTF_8));
            // 时间加一天
            calendar.add(Calendar.DATE, 1);
        }
        // 创建指定范围内的key
        String redisKey = RedisKeyUtil.getDAU(df.format(start), df.format(end));
        // 进行不同日期之间数据的合并(or)操作
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 将所有日期下的dau进行汇总到redisKey当中
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(StandardCharsets.UTF_8),
                        dateList.toArray(new byte[0][0])); // 转成为一个二位的数组
                // 统计总数
                return connection.bitCount(redisKey.getBytes(StandardCharsets.UTF_8));
            }
        });
    }


}
