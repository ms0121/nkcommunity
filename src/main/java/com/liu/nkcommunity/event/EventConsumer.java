package com.liu.nkcommunity.event;

import com.alibaba.fastjson.JSONObject;
import com.liu.nkcommunity.domain.Event;
import com.liu.nkcommunity.domain.Message;
import com.liu.nkcommunity.service.MessageService;
import com.liu.nkcommunity.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息消费者：消费者会自动的进行监控消息队列中的数据信息，只有有数据就会自动的进行消费
 */
@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    //@Autowired
    //  private RabbitTemplate rabbitTemplate;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // message的形式; {"data":{},"entityId":222,"entityType":0,"entityUserId":0,"topic":"comment","userId":111}
//    @RabbitListener(bindings = {
//            @QueueBinding(
//                    value = @Queue, // 使用的时主题模式，默认使用的交换机名字时 topics
//                    exchange = @Exchange(type = "direct", value = "directs", durable = "false"), // 指定交换机名称和类型
//                    // 只要匹配到当下的任意一个key都会触发消费的事件
//                    key = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW}
//            )
//    })

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleCommentMessage(String msg) {
        if (StringUtils.isBlank(msg)) {
            LOGGER.error("消息的内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(msg, Event.class);
        if (event == null) {
            LOGGER.error("消息格式错误!");
            return;
        }

        // 发送站内通知消息(因为别人给帖子、评论、进行了点赞，那么由系统通知对应的用户别人给你评论或者点赞了)
        Message message = new Message();
        // 可以理解为张三给李四进行点赞
        message.setFromId(SYSTEM_USER_ID); // 张三
        message.setToId(event.getEntityUserId()); // 李四
        // 会话存储的是主题的路由key
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        // 构造Message消息对象的content信息
        HashMap<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        // 判断事件对象中是否存在其他的数据信息
        if (event.getData().isEmpty()) {
            // 获取到每一个(key,value)结构的数据信息
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        // 将消息添加到数据库中
        messageService.addMessage(message);

    }
}
