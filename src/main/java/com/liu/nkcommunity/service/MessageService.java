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
}
