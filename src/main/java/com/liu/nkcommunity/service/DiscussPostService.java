package com.liu.nkcommunity.service;

import com.liu.nkcommunity.domain.DiscussPost;

import java.util.List;

public interface DiscussPostService {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    int selectDiscussPostRows(int userId);

}
