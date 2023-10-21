package com.zzclearning.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author bling
 * @create 2023-02-06 9:10
 */
@Configuration
public class GulimallESconfig {
    public static final RequestOptions COMMON_OPTIONS;

    /**
     * 通用设置项
     */
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        //        builder.addHeader("Authorization", "Bearer " + TOKEN);
        //        builder.setHttpAsyncResponseConsumerFactory(
        //                new HttpAsyncResponseConsumerFactory
        //                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }
    @Bean
    public RestHighLevelClient esRestClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.56.10",9200,"http")));
    }
}
