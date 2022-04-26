package com.liu.nkcommunity.mapper.repository;

import com.liu.nkcommunity.domain.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 会自动生成相应的接口
 * 需要指定查询的索引类型，主键的类型
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}
