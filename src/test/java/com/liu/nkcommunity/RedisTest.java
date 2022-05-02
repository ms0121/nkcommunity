package com.liu.nkcommunity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RedisTest extends NkcommunityApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * string类型
     */
    @Test
    public void testString() {
        String redisKey = "test:count";
        // 存值
        redisTemplate.opsForValue().set(redisKey, 1);
        // 取值
        System.out.println("redisTemplate.opsForValue().get(redisKey) = " + redisTemplate.opsForValue().get(redisKey));
        // 加1
        System.out.println("redisTemplate.opsForValue().increment(redisKey) = " + redisTemplate.opsForValue().increment(redisKey));
        // 减1
        System.out.println("redisTemplate.opsForValue().decrement(redisKey) = " + redisTemplate.opsForValue().decrement(redisKey));

    }

    @Test
    public void testHash() {
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey, "id", 1001);
        redisTemplate.opsForHash().put(redisKey, "name", "zhangsan");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "name"));
    }

    @Test
    public void testList() {
        String redisKey = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println("redisTemplate.opsForList().size(redisKey) = " + redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }


    @Test
    public void testSet() {
        String redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey, "1", "2", "3", "4", "5");
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }


    // 有序集合()
    @Test
    public void testSortSet() {
        String redisKey = "test:student";
        // 值 得分
        redisTemplate.opsForZSet().add(redisKey, "a", 80);
        redisTemplate.opsForZSet().add(redisKey, "b", 93);
        redisTemplate.opsForZSet().add(redisKey, "c", 86);
        redisTemplate.opsForZSet().add(redisKey, "d", 89);
        redisTemplate.opsForZSet().add(redisKey, "e", 79);

        // 获取a的排名
        System.out.println(redisTemplate.opsForZSet().rank(redisKey, "a"));
        System.out.println("redisTemplate.opsForZSet().zCard(redisKey) = " + redisTemplate.opsForZSet().zCard(redisKey));
        // 获取0-2的排名
        System.out.println("redisTemplate.opsForZSet().range(redisKey, 0, 2) = " + redisTemplate.opsForZSet().range(redisKey, 0, 2));
        // 获取b的得分
        System.out.println("redisTemplate.opsForZSet().score(redisKey, \"b\") = " + redisTemplate.opsForZSet().score(redisKey, "b"));
    }


    @Test
    public void testOther(){
        String redisKey = "test:student";
        // 删除key
        System.out.println("redisTemplate.delete(redisKey) = " + redisTemplate.delete(redisKey));

        // 设置key的过期时间
        redisTemplate.expire("test:teachers", 10, TimeUnit.SECONDS);
    }


    // 多次访问同一个key
    @Test
    public void testBoundOperation(){
        String redisKey = "test:count";
        // 绑定一个key
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println("operations.get() = " + operations.get());
    }


    // redis的事务处理（编程式事务）
    @Test
    public void tesTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                // 开启事务操作
                redisTemplate.multi();
                operations.opsForSet().add(redisKey, "a");
                operations.opsForSet().add(redisKey, "b");
                operations.opsForSet().add(redisKey, "c");
                System.out.println("operations.opsForSet().members(redisKey) = " + operations.opsForSet().members(redisKey));
                // 提交事务操作
                return operations.exec();
            }
        });
        System.out.println("obj = " + obj);
    }


    /**
     * 测试hyperloglog数据类型
     */
    @Test
    public void testHyperLoglog() {
        String redisKey1 = "test1:l1:01";
        for (int i = 0; i < 5000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey1, i);
        }
        for (int i = 0; i < 5000; i++) {
            int r = (int) (Math.random() * 5000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey1, r);
        }
        Long size = redisTemplate.opsForHyperLogLog().size(redisKey1);
        System.out.println("size = " + size);
    }


    @Test
    public void testHyperloglog2(){
        String redisKey1 = "test1:l2:01";
        for (int i = 0; i < 5000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey1, i);
        }

        String redisKey2 = "test1:l2:02";
        for (int i = 2500; i < 7500; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }
        String redisKey3 = "test1:l2:03";
        for (int i = 5000; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }
        // 计算总共有多少个不重复的数
        String redisKey4 = "test1:l2:04";
        // 理论上有10000个不同的值，总数15000个,
        // 数据合并
        redisTemplate.opsForHyperLogLog().union(redisKey4, redisKey1, redisKey2, redisKey3);

        // 统计总数
        Long size = redisTemplate.opsForValue().size(redisKey4);
        System.out.println("size = " + size);
    }


    /**
     * 测试bitmap类型的数据信息
     */
    @Test
    public void testBitmap() {
        String redisKey = "test1:l3:01";
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 7));

        // 统计指定key的bit中为true的数据大小
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes(StandardCharsets.UTF_8));
            }
        });

        System.out.println(obj);
    }


    /**
     * 计算bitmap类型的or运算
     */
    @Test
    public void testBitmap2() {
        String redisKey1 = "test1:l3:01";
        String redisKey2 = "test1:l3:02";
        String redisKey3 = "test1:l3:03";
        String redisKey4 = "test1:l3:04";

        // 给不同的key设置不同位置的偏移量
        redisTemplate.opsForValue().setBit(redisKey1, 0, true);
        redisTemplate.opsForValue().setBit(redisKey1, 1, true);
        redisTemplate.opsForValue().setBit(redisKey1, 2, true);

        redisTemplate.opsForValue().setBit(redisKey2, 2, true);
        redisTemplate.opsForValue().setBit(redisKey2, 3, true);
        redisTemplate.opsForValue().setBit(redisKey2, 4, true);

        redisTemplate.opsForValue().setBit(redisKey3, 4, true);
        redisTemplate.opsForValue().setBit(redisKey3, 5, true);
        redisTemplate.opsForValue().setBit(redisKey3, 6, true);

        // 计算或运算
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 设置选择的是bit的或(or)运算，将key1，2，3中的数据合并到4中
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey4.getBytes(StandardCharsets.UTF_8),
                        redisKey1.getBytes(StandardCharsets.UTF_8),
                        redisKey2.getBytes(StandardCharsets.UTF_8),
                        redisKey3.getBytes(StandardCharsets.UTF_8));
                // 统计结果
                return connection.bitCount(redisKey4.getBytes(StandardCharsets.UTF_8));
            }
        });
        // 将输出结果进行相应的设置，
        System.out.println("obj = " + obj);
    }
}







