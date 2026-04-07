package org.ys.transaction.Infrastructure.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient(
            @Value("${app.es.host:localhost}") String host,
            @Value("${app.es.port:9200}") Integer port,
            @Value("${app.es.scheme:http}") String scheme
    ) {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, scheme))
        );
    }
}
