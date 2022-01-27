package com.liu.nkcommunity.mapper;

import com.liu.nkcommunity.domain.Message;

import java.util.List;

/**
 * 私信列表的实现步骤：
 *  1、私信列表
 *      - 查询当前用户的会话列表
 *      - 每个会话只显示一条最新的私信
 *      - 支持分页显示
 *
 *  2、私信详情
 *      - 查询某个会话所包含的私信
 *      - 支持分页显示
 */
public interface MessageMapper {

    // 查询当前用户的会话列表,针对每一个会话只显示最新的一条信息
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset,int limit);

    // 查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);
}
