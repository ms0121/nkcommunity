package com.liu.nkcommunity;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 模拟实现阻塞队列：生产者和消费者
 */
public class BlockingQueueTest {

    public static void main(String[] args) {
        ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(10);
        // 一个生产者 三个消费者
        new Thread(new Producer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
    }
}

// 生产者
class Producer implements Runnable {

    private BlockingQueue<Integer> queue;

    public Producer(BlockingQueue queue) {
        this.queue = queue;
    }

    // 生产者总共生产100个产品
    @Override
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + " 生产了， 商品数量: " + queue.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//消费者
class Consumer implements Runnable {

    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue queue) {
        this.queue = queue;
    }

    // 生产者总共生产100个产品
    @Override
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                Thread.sleep(new Random().nextInt(1000));
                // 消费队首元素
                queue.take();
                System.out.println(Thread.currentThread().getName() + " 消费了， 商品数量还剩余: " + queue.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




















