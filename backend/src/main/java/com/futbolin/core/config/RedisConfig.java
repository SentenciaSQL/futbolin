package com.futbolin.core.config;

import com.futbolin.core.props.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true")
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory, AppProperties properties) {
        return new StringRedisTemplate(factory);
    }
}
