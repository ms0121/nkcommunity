package com.liu.nkcommunity.config;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

@Configuration
public class EsConfig extends AbstractElasticsearchConfiguration {

//    // 创建连接
    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("101.43.133.25:9200")
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
}
