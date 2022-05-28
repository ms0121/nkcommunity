package com.liu.nkcommunity.config;

import com.liu.nkcommunity.job.AlphaJob;
import com.liu.nkcommunity.job.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author lms
 * @date 2022-05-14 - 17:36
 */
@Configuration
public class QuartzConfig {
    /**
     * factoryBean可以简化Bean的实例化过程
     * 1. 通过factoryBean封装Bean的实例化过程
     * 2. 将FactoryBean装配到Spring容器中
     * 3. 将FactoryBean注入给其他的Bean
     * 4. 该bean得到的是FactoryBean所管理的对象实例
     */
    // 配置JobDetail
//    @Bean
//    public JobDetailFactoryBean alphaDetail(){
//        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
//        factoryBean.setJobClass(AlphaJob.class);
//        factoryBean.setName("alphaJob");
//        factoryBean.setGroup("alphaGroup");
//        factoryBean.setDurability(true);
//        factoryBean.setRequestsRecovery(true);
//        return factoryBean;
//    }
//
//    // 配置触发器trigger
//    @Bean
//    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaDetail) {
//        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
//        factoryBean.setJobDetail(alphaDetail);
//        factoryBean.setName("alphaTrigger");
//        factoryBean.setGroup("alphaTriggerGroup");
//        factoryBean.setRepeatInterval(3000); // 3s
//        factoryBean.setJobDataMap(new JobDataMap()); // 数据保存的数据类型
//        return factoryBean;
//    }


    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    // 配置触发器trigger
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshJobTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5); // 5分钟
        factoryBean.setJobDataMap(new JobDataMap()); // 数据保存的数据类型
        return factoryBean;
    }
}
