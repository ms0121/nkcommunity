package com.liu.nkcommunity;


import com.alibaba.fastjson.JSONObject;
import com.liu.nkcommunity.domain.DiscussPost;
import com.liu.nkcommunity.mapper.DiscussPostMapper;
import com.liu.nkcommunity.mapper.repository.DiscussPostRepository;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.io.IOException;
import java.util.Map;

public class ElasticSearchTest extends NkcommunityApplicationTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 查询帖子
     * GET /discusspost/_search
     * {
     *   "query":{
     *     "multi_match": {
     *       "query": "互联网",
     *       "fields": ["title", "content"]
     *     }
     *   },
     *   "from": 0,
     *   "size": 2,
     *   "sort": [
     *     {
     *       "type": {
     *         "order": "desc"
     *       },
     *       "score": {
     *         "order": "desc"
     *      },
     *      "createTime": {
     *        "order": "desc"
     *      }
     *     }
     *   ],
     *   "highlight": {
     *   "require_field_match": "false",
     *     "pre_tags": ["<em>"],
     *     "post_tags": ["</em>"],
     *     "fields": {"title": {}, "content": {}}
     *   }
     * }
     * @throws IOException
     */
    @Test
    public void searchByRepository() {
        // 构建查询对象
//        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
//                // 在哪些字段上查询指定的关键词
//                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
//                // 只当排序的方式,先按照类型，得分，创建时间进行排序
//                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
//                // 指定返回的条数
//                .withPageable(PageRequest.of(0, 10))
//                // 哪些字段要显示高亮，并给需要高亮度的字段添加指定的前后缀
//                .withHighlightFields(
//                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
//                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
//                ).build();
        // 设置需要查询的索引
        SearchRequest searchRequest = new SearchRequest("discusspost");
        // 构建查询的条件信息
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 设置需要在哪些字段中进行查询
        highlightBuilder.requireFieldMatch(false).field("title").field("content")
                .preTags("<em>")
                .postTags("</em>");
        // 拼接查询条件
        sourceBuilder.query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .from(0)
                .size(10)
                .sort("type", SortOrder.DESC)
                .sort("score", SortOrder.DESC)
                .sort("createTime", SortOrder.DESC)
                // 都是数组， 参数1：需要返回的字段(不填默认返回所有)  参数2：需要排序的字段
                .fetchSource(new String[]{}, new String[]{})
                .highlighter(highlightBuilder);
        // 将查询条件设置到查询请求中
        searchRequest.source(sourceBuilder);
        // 查询
        SearchResponse response = null;
        try {
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // System.out.println("记录总条数: " + response.getHits().getTotalHits().value);
        // System.out.println("记录的最大得分: " + response.getHits().getMaxScore());

        // 所有的文档信息
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            // 如何
            // System.out.println("id: " + hit.getId() + ",source: " + hit.getSourceAsString());
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            sourceAsMap.remove("_class");
            // 获取高亮的字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields.containsKey("content")){
                sourceAsMap.put("content", highlightFields.get("content").getFragments()[0].toString());
                // System.out.println("content 高亮的结果 " + highlightFields.get("content").getFragments()[0].toString());
            }
            if (highlightFields.containsKey("title")){
                sourceAsMap.put("title", highlightFields.get("title").getFragments()[0].toString());
                // System.out.println("title 高亮的结果 " + highlightFields.get("title").getFragments()[0].toString());
            }
            // System.out.println(sourceAsMap);
            DiscussPost discussPost = JSONObject.parseObject(JSONObject.toJSONString(sourceAsMap), DiscussPost.class);
            System.out.println("discussPost = " + discussPost);
        }

    }

}
