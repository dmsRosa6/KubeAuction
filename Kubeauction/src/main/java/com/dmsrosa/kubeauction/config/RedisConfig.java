package com.dmsrosa.kubeauction.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

    public static final int DEFAULT_TTL = 1;
    public static final int BIDS_DEFAULT_TTL = 1;
    public static final int AUCTIONS_DEFAULT_TTL = 1;
    public static final int USERS_DEFAULT_TTL = 1;

    public static final String BIDS_DEFAULT_PREFIX = "bidCache";
    public static final String AUCTIONS_DEFAULT_PREFIX = "auctionCache";
    public static final String USERS_DEFAULT_PREFIX = "userCache";

    public static final String DELIM = "::";

    public static final String BIDS_PREFIX_DELIM = BIDS_DEFAULT_PREFIX + DELIM;
    public static final String AUCTIONS_PREFIX_DELIM = AUCTIONS_DEFAULT_PREFIX + DELIM;
    public static final String USERS_PREFIX_DELIM = USERS_DEFAULT_PREFIX + DELIM;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(DEFAULT_TTL))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        // cacheConfigs.put("auctionCache",
        // defaultConfig.entryTtl(Duration.ofMinutes(AUCTIONS_DEFAULT_TTL)));
        // cacheConfigs.put("bidCache",
        // defaultConfig.entryTtl(Duration.ofMinutes(BIDS_DEFAULT_TTL)));
        // cacheConfigs.put("userCache",
        // defaultConfig.entryTtl(Duration.ofMinutes(USERS_DEFAULT_TTL)));

        return RedisCacheManager.builder(factory)
                .withInitialCacheConfigurations(cacheConfigs)
                .cacheDefaults(defaultConfig)
                .build();
    }
}
