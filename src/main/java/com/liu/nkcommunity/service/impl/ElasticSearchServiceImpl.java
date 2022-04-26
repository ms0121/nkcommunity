package com.liu.nkcommunity.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.liu.nkcommunity.domain.DiscussPost;
import com.liu.nkcommunity.mapper.DiscussPostMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 实现搜索的功能
 *  搜索服务
 *      将贴子保存至elasticSearch服务器
 *      从elasticSearch服务器删除帖子
 *      从elasticSearch服务搜索帖子
 *
 *  发布事件：
 *      发布帖子的时候，将贴子异步到es服务器中
 *      增加评论的时候，将贴子异步到es服务器中
 *      在消费者组件中增加一个方法，消费帖子发布事件
 *
 *  显示结果：
 *      在控制器中处理请求，在html上显示搜索的结果
 *
 * @author: lms
 * @date: 2022-04-26 22:16
 */
@Service
public class ElasticSearchServiceImpl {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    // 添加帖子
    public void save(DiscussPost discussPost) {
        elasticsearchOperations.save(discussPost);
    }

    // 删除帖子
    public void deleteById(int id) {
        DiscussPost discussPost = discussPostMapper.findDiscussPostById(id);
        elasticsearchOperations.delete(discussPost);
    }

    /**
     * 查看帖子：
     * GET /discusspost/_search
     * {
     *   "query":{
     *     "multi_match": {
     *       "query": "互联网",
     *       "fields": ["title", "content"]
     *     }
     *   },
     *   "from": 0,
     *   "size": 10,
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
     * @param keyword
     * @param current
     * @param limit
     * @return
     */
    public Map<String, Object> searchDiscussPost(String keyword, int current, int limit) {
        // 设置需要查询的索引
        SearchRequest searchRequest = new SearchRequest("discusspost");
        // 构建查询的条件信息
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 设置需要在哪些字段中进行高亮显示
        highlightBuilder.requireFieldMatch(false).field("title").field("content")
                .preTags("<em>")
                .postTags("</em>");
        // 拼接查询条件
        sourceBuilder.query(
                // 在哪些字段上进行匹配查询的数据信息
                QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .from(current)  // 起始页
                .size(limit) // 每页显示的数量
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

        Map<String, Object> map = new HashMap<>();
        // 总记录数目
        long total = response.getHits().getTotalHits().value;
        map.put("total", total);

        // 封装查询得到的discussPost对象
        List<DiscussPost> discussPostList = new ArrayList<>();
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
            DiscussPost discussPost = JSONObject.parseObject(JSONObject.toJSONString(sourceAsMap), DiscussPost.class);
            // System.out.println("discussPost = " + discussPost);
            discussPostList.add(discussPost);
        }
        map.put("discussPostList", discussPostList);
        return map;
    }

}



















