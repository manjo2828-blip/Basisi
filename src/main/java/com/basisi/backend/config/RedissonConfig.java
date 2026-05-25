package com.basisi.backend.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "reservation.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedissonConfig {

    @Value("${reservation.lock.redis-address:redis://127.0.0.1:6379}")
    private String redisAddress;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(redisAddress);
        return Redisson.create(config);
    }
}

