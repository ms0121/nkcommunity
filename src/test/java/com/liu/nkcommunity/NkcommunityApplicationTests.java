package com.liu.nkcommunity;

import com.liu.nkcommunity.domain.DiscussPost;
import com.liu.nkcommunity.mapper.DiscussPostMapper;
import com.liu.nkcommunity.mapper.repository.DiscussPostRepository;
import com.sun.corba.se.impl.dynamicany.DynValueBoxImpl;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

@SpringBootTest
class NkcommunityApplicationTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

//    @Autowired
//    private ElasticsearchTemplate elasticsearchRestTemplate;

    @Test
    public void searchByRepository(){
        // 构建查询对象
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                // 在哪些字段上查询指定的关键词
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // 只当排序的方式,先按照类型，得分，创建时间进行排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 指定返回的条数
                .withPageable(PageRequest.of(0, 10))
                // 哪些字段要显示高亮，并给需要高亮度的字段添加指定的前后缀
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        SearchHits<DiscussPost> discussPostSearchHits = elasticsearchOperations.search(searchQuery, DiscussPost.class);
        System.out.println(discussPostSearchHits.getMaxScore());
        System.out.println(discussPostSearchHits.getTotalHits());
    }



    @Test
    public void insertTest() {
        discussPostRepository.save(discussPostMapper.findDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.findDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.findDiscussPostById(243));
    }

    @Test
    public void batchInsertTest(){
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0,100));
    }


}
