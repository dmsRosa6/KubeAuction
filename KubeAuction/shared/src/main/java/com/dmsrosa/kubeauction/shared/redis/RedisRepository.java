package com.dmsrosa.kubeauction.shared.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.shared.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.shared.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;
import com.dmsrosa.kubeauction.shared.database.domain.Bid;
import com.dmsrosa.kubeauction.shared.database.domain.User;
import com.dmsrosa.kubeauction.shared.utils.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class RedisRepository {
    public static final int DEFAULT_TTL = 1;
    public static final int BIDS_DEFAULT_TTL = 1;
    public static final int AUCTIONS_DEFAULT_TTL = 1;
    public static final int USERS_DEFAULT_TTL = 1;

    public static final String BIDS_PREFIX_DELIM = "cache::bid::";
    public static final String AUCTIONS_PREFIX_DELIM = "cache::auction::";
    public static final String USERS_PREFIX_DELIM = "cache::user::";
    public static final String USERS_PREFIX_DELIM_EMAIL = "cache::user::email::";

    public static final String DELIM = "::";
    public static final String AUCTIONS_NOTIFICATIONS_KEY = "auctions::notifications";
    public static final String CHANNEL_PREFIX = "channel::auction::";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ValueOperations<String, Object> valueOps;

    public RedisRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.valueOps = redisTemplate.opsForValue();
    }

    public <T> T redisGetOrThrow(String id, Class<T> type, boolean usesEmail) {
        T val = redisGet(id, type, usesEmail);
        if (val == null) {
            throw new IllegalStateException("Redis cache miss for key: " + buildKey(id, type, usesEmail));
        }
        return val;
    }

    public <T> T redisGet(String id, Class<T> type, boolean usesEmail) {
        String key = buildKey(id, type, usesEmail);
        Object json = valueOps.get(key);
        if (json == null)
            return null;
        return objectMapper.convertValue(json, type);
    }

    public <T> T redisGet(String id, Class<T> type) {
        return redisGet(id, type, false);
    }

    public void redisSet(String id, Object value, boolean usesEmail) {
        String key = buildKey(id, value, usesEmail);
        try {
            String json = objectMapper.writeValueAsString(value);
            valueOps.set(key, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to write Redis value for key: " + key, e);
        }
    }

    public void redisSet(String id, Object value) {
        redisSet(id, value, false);
    }

    public void redisMultiSet(Map<String, Object> map) {
        Map<String, Object> redisMap = new HashMap<>();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = buildKey(en.getKey(), en.getValue(), false);
            try {
                redisMap.put(key, objectMapper.writeValueAsString(en.getValue()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed multiSet for key: " + key, e);
            }
        }
        valueOps.multiSet(redisMap);
    }

    public void redisMultiSetWithVariants(Map<String, Pair<Object, Boolean>> map) {
        Map<String, Object> redisMap = new HashMap<>();
        for (var en : map.entrySet()) {
            String key = buildKey(en.getKey(), en.getValue().getFirst(), en.getValue().getSecond());
            try {
                redisMap.put(key, objectMapper.writeValueAsString(en.getValue().getFirst()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed multiSet variant for key: " + key, e);
            }
        }
        valueOps.multiSet(redisMap);
    }

    public <T> void redisDelete(String id, Class<T> type, boolean usesEmail) {
        String key = buildKey(id, type, usesEmail);
        redisTemplate.delete(key);
    }

    public <T> void redisDelete(String id, Class<T> type) {
        redisDelete(id, type, false);
    }

    public void addToExpirationZSet(String id, long score) {
        redisTemplate.opsForZSet()
                .add(AUCTIONS_PREFIX_DELIM + id, id, score);
    }

    public Set<TypedTuple<Object>> getExpiredFromZSet(String key, long maxScore) {
        return redisTemplate.opsForZSet()
                .rangeByScoreWithScores(key, 0, maxScore);
    }

    public void deleteFromExpirationZSet(String key, String id) {
        redisTemplate.opsForZSet().remove(key, id);
    }

    public void addToNotifications(Object value, double score) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForZSet().add(AUCTIONS_NOTIFICATIONS_KEY, json, score);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed notifications ZSet for key: " + AUCTIONS_NOTIFICATIONS_KEY, e);
        }
    }

    public void deleteFromNotifications(Object value) {
        redisTemplate.opsForZSet().remove(AUCTIONS_NOTIFICATIONS_KEY, value);
    }

    public Set<TypedTuple<Object>> getDueNotifications() {
        long now = System.currentTimeMillis();
        return redisTemplate.opsForZSet().rangeByScoreWithScores(AUCTIONS_NOTIFICATIONS_KEY, 0, now);
    }

    public void publishToAuctionChannel(String auctionId, Object message) {
        String channel = CHANNEL_PREFIX + auctionId;
        try {
            String payload = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(channel, payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to publish to channel: " + channel, e);
        }
    }

    private String buildKey(String id, Object value, boolean usesEmail) {
        String prefix;
        if (value instanceof Auction || value instanceof AuctionEntity) {
            prefix = AUCTIONS_PREFIX_DELIM;
        } else if (value instanceof Bid || value instanceof BidEntity) {
            prefix = BIDS_PREFIX_DELIM;
        } else if (value instanceof User || value instanceof UserEntity) {
            prefix = usesEmail ? USERS_PREFIX_DELIM_EMAIL : USERS_PREFIX_DELIM;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass());
        }
        return prefix + id;
    }

    private String buildKey(String id, Class<?> type, boolean usesEmail) {
        String prefix;
        if (type == Auction.class || type == AuctionEntity.class) {
            prefix = AUCTIONS_PREFIX_DELIM;
        } else if (type == Bid.class || type == BidEntity.class) {
            prefix = BIDS_PREFIX_DELIM;
        } else if (type == User.class || type == UserEntity.class) {
            prefix = usesEmail ? USERS_PREFIX_DELIM_EMAIL : USERS_PREFIX_DELIM;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type.getName());
        }
        return prefix + id;
    }
}