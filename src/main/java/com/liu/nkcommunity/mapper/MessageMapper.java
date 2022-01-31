package com.liu.nkcommunity.mapper;

import com.liu.nkcommunity.domain.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 私信列表的实现步骤：
 * 1、私信列表
 * - 查询当前用户的会话列表
 * - 每个会话只显示一条最新的私信
 * - 支持分页显示
 * <p>
 * 2、私信详情
 * - 查询某个会话所包含的私信
 * - 支持分页显示
 */
@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表,针对每一个会话只显示最新的一条信息
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    // 新增消息
    int insertMessage(Message message);

    // 修改消息的状态（比如从未读状态设置为已读）
    int updateStatus(List<Integer> ids, int status);

    // 查询指定用户的某个主题下最新的通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读的通知的数量
    int selectNoticeUnreadCount(int userId, String topic);




}
