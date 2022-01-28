package com.liu.nkcommunity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;

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

}







