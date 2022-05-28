package com.liu.nkcommunity.job;

import com.liu.nkcommunity.domain.DiscussPost;
import com.liu.nkcommunity.service.DiscussPostService;
import com.liu.nkcommunity.service.LikeService;
import com.liu.nkcommunity.service.impl.ElasticSearchServiceImpl;
import com.liu.nkcommunity.util.CommunityConstant;
import com.liu.nkcommunity.util.RedisKeyUtil;
import org.elasticsearch.client.RestHighLevelClient;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 定时计算帖子的分数，从而实现排行榜
 * @author lms
 * @date 2022-05-28 - 13:26
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostScoreRefreshJob.class);


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticSearchServiceImpl elasticSearchService;

    @Autowired
    private LikeService likeService;

    // 初始化时间的纪元（只需要初始化一次）
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-05-28 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化时间纪元失败！");
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        // 获取到当前key对应的数据集合
        BoundSetOperations operations = redisTemplate.boundSetOps(postScoreKey);
        // 没有帖子进行更新
        if (operations.size() == 0) {
            LOGGER.info("任务取消，没有需要进行刷新的帖子!");
            return;
        }

        LOGGER.info("任务开始！正在刷新帖子的分数： " + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer)operations.pop());
        }
        LOGGER.info("任务结束！帖子分数刷新完毕！");
    }

    // 刷新帖子的分数
    private void refresh(Integer postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            LOGGER.error("该帖子不存在： id = " + postId);
            return;
        }
        // 评论的数量
        int commentCount = post.getCommentCount();
        // 帖子的点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        // 计算权重
        double w = commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log(Math.max(w, 1)) +
                (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子的分数
        discussPostService.updateScore(postId, score);
        post.setScore(score);
        // 同步搜索引擎中的数据
        elasticSearchService.save(post);
    }
}
