package com.liu.nkcommunity;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author lms
 * @date 2022-05-14 - 16:44
 */

@Slf4j
public class ThreadPoolTest extends NkcommunityApplicationTests {


    /**
     * 测试1：jdk的线程池
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    // 创建可以执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(5);

    @Test
    public void testJdk1() {
        Thread task = new Thread(() -> {
           log.info("测试1-----");
        });
        for (int i = 0; i < 4; i++) {
            executorService.submit(task);
        }
    }


    @Test
    public void testJdk2() throws InterruptedException {
        Thread task = new Thread(() -> {
            log.info("测试2-----> ");
        });
        for (int i = 0; i < 5; i++) {
            scheduledExecutorService.scheduleAtFixedRate(task, 1000, 2000, TimeUnit.MILLISECONDS);
        }
        Thread.sleep(10000);
    }






}
