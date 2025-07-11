package com.dmsrosa.kubeauction.shared.redis;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.shared.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.shared.database.dao.entity.PopularAuctionEntity;
import com.dmsrosa.kubeauction.shared.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;
import com.dmsrosa.kubeauction.shared.database.domain.Bid;
import com.dmsrosa.kubeauction.shared.database.domain.PopularAuction;
import com.dmsrosa.kubeauction.shared.database.domain.User;
import com.dmsrosa.kubeauction.shared.utils.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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

    public static final String POPULAR_AUCTIONS = "popular_Auctions";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ValueOperations<String, Object> valueOps;

    public RedisRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.valueOps = redisTemplate.opsForValue();
    }

    public void setPopular(List<PopularAuctionEntity> list) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(list);
        valueOps.set(POPULAR_AUCTIONS, json);
    }

    public List<PopularAuctionEntity> getPopular() {
        String json = (String) valueOps.get(POPULAR_AUCTIONS);
        if (json == null) {
            throw new IllegalStateException("Redis cache miss for popular auctions");
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<PopularAuctionEntity>>() {
                    });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse popular auctions JSON", e);
        }
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
        Object value = valueOps.get(key);
        if (value == null)
            return null;

        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            return objectMapper.convertValue(value, type);
        }
    }

    public <T> T redisGet(String id, Class<T> type) {
        return redisGet(id, type, false);
    }

    public void redisSet(String id, Object value, boolean usesEmail) {
        String key = buildKey(id, value, usesEmail);
        valueOps.set(key, value);
    }

    public void redisSet(String id, Object value) {
        redisSet(id, value, false);
    }

    public void redisMultiSet(Map<String, Object> map) {
        Map<String, Object> redisMap = new HashMap<>();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = buildKey(en.getKey(), en.getValue(), false);
            redisMap.put(key, en.getValue());
        }
        valueOps.multiSet(redisMap);
    }

    public void redisMultiSetWithVariants(Map<String, Pair<Object, Boolean>> map) {
        Map<String, Object> redisMap = new HashMap<>();
        for (var en : map.entrySet()) {
            String key = buildKey(en.getKey(), en.getValue().getFirst(), en.getValue().getSecond());
            redisMap.put(key, en.getValue().getFirst());
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

    public void addToNotifications(Object value, double score) {
        redisTemplate.opsForZSet().add(AUCTIONS_NOTIFICATIONS_KEY, value, score);
    }

    public void deleteFromNotifications(Object value) {
        redisTemplate.opsForZSet().remove(AUCTIONS_NOTIFICATIONS_KEY, value);
    }

    public Set<TypedTuple<Object>> getDueNotifications() {
        long now = System.currentTimeMillis();
        return redisTemplate.opsForZSet().rangeByScoreWithScores(AUCTIONS_NOTIFICATIONS_KEY, 0, now);
    }

    public void publishToAuctionChannel(String id, Object message) {
        String channel = CHANNEL_PREFIX + id;
        redisTemplate.convertAndSend(channel, message);
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