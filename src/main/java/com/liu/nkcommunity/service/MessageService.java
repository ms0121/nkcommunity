package com.liu.nkcommunity.service;

import com.liu.nkcommunity.domain.Message;

import java.util.List;

public interface MessageService {
    // 查询当前用户的会话列表,针对每一个会话只显示最新的一条信息
    List<Message> findConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int findConversationCount(int userId);

    // 查询某个会话所包含的私信列表
    List<Message> findLetters(String conversationId, int offset,int limit);

    // 查询某个会话所包含的私信数量
    int findLetterCount(String conversationId);

    // 查询未读私信的数量
    int findLetterUnreadCount(int userId, String conversationId);

    // 新增消息
    int addMessage(Message message);

    // 修改消息的状态（比如从未读状态设置为已读）
    int readMessage(List<Integer> ids);

    // 查询指定用户的某个主题下最新的通知
    Message findLatestNotice(int userId, String topic);

    // 查询某个主题包含的通知数量
    int findNoticeCount(int userId, String topic);

    // 查询未读的通知数量
    int findNoticeUnreadCount(int userId, String topic);
}
