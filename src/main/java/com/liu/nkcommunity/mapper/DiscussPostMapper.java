package com.liu.nkcommunity.mapper;

import com.liu.nkcommunity.domain.DiscussPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DiscussPostMapper {

    // 如果userId为空，表示查询所有的讨论贴，否则表示当前当前指定用户的帖子
    // 并进行相应的分页功能
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param注解用于给参数取别名
    // 如果当前的方法只有一个参数，并且使用在动态的sql <if>语句当中，则必须使用 @Param添加别名，否则会报错
    int selectDiscussPostRows(@Param("userId") int userId);


}
