package com.liu.nkcommunity.event;

import com.alibaba.fastjson.JSONObject;
import com.liu.nkcommunity.domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 事件生产者
 * - 触发事件：
 * - 评论后，发布通知
 * - 点赞后，发布通知
 * - 关注后，发布通知
 * - 处理事件：
 * - 封装事件对象
 * - 开发事件的生产者
 * - 开发事件的消费者
 */
@Component
public class EventProducer {

//    @Autowired
//    private RabbitTemplate rabbitTemplate;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void fireEvent(Event event) {
        // 将事件发送到指定交换机的不同路由中，其中把内容转换为json对象，消费者受到json后，可以将json转换成Event
        // rabbitTemplate.convertAndSend("directs",event.getTopic(), JSONObject.toJSONString(event));
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
