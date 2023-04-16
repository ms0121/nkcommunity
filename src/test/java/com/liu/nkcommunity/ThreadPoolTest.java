package com.liu.nkcommunity;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @description:
 * @author: lms
 * @date: 2022-05-03 11:35
 */
//@Slf4j
@SpringBootTest
public class ThreadPoolTest extends NkcommunityApplicationTests {

    private static Logger logger = LoggerFactory.getLogger(ThreadPoolTest.class);

    // 使用的是无界队列
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);

    // 执行定时任务的线程池
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    public void sleep(long n) {
        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJDK() {
        // 创建任务
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello executors");
            }
        };

        // 使用线程池执行任务
        for (int i = 0; i < 10; i++) {
            executorService.submit(runnable);
        }
        // 休眠10秒钟
        sleep(10000);
    }


}
