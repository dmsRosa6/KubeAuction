package com.dmsrosa.kubeauction.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer(mapper);
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }

}
