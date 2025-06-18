package com.dmsrosa.kubeauction.shared.redis;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;

import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.shared.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.shared.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;
import com.dmsrosa.kubeauction.shared.database.domain.Bid;
import com.dmsrosa.kubeauction.shared.database.domain.User;
import com.dmsrosa.kubeauction.shared.utils.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//TODO GET SOMETHING FROM REDIS INSTEAD OF TROWING THE RESPONSE
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

    private static final String AUCTIONS_EXPIRATION_KEY = "auctions::expiration";

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    public RedisRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> T redisGet(String id, Class<T> type, boolean usesEmail) {
        String key = "null";
        try {
            String prefix = "";

            if (type == Auction.class || type == AuctionEntity.class) {
                prefix = AUCTIONS_PREFIX_DELIM;
            } else if (type == Bid.class || type == BidEntity.class) {
                prefix = BIDS_PREFIX_DELIM;
            } else if (type == User.class || type == UserEntity.class) {
                if (usesEmail)
                    prefix = USERS_PREFIX_DELIM_EMAIL;
                else
                    prefix = USERS_PREFIX_DELIM;
            } else {
                throw new IllegalArgumentException("Unsupported type: " + type.getName());
            }

            key = prefix + id;

            Object json = redisTemplate.opsForValue().get(key);
            if (json == null)
                return null;

            return objectMapper.convertValue(json, type);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to read Redis value for key: " + key, e);
        }
    }

    public <T> T redisGet(String id, Class<T> type) {
        return redisGet(id, type, false);
    }

    public void redisSet(String id, Object value, boolean usesEmail) {
        String key = "null";

        try {
            String prefix = "";

            if (value instanceof Auction || value instanceof AuctionEntity) {
                prefix = AUCTIONS_PREFIX_DELIM;
            } else if (value instanceof Bid || value instanceof BidEntity) {
                prefix = BIDS_PREFIX_DELIM;
            } else if (value instanceof User || value instanceof UserEntity) {
                if (usesEmail)
                    prefix = USERS_PREFIX_DELIM_EMAIL;
                else
                    prefix = USERS_PREFIX_DELIM;
            } else {
                throw new IllegalArgumentException("Unsupported type: " + value.getClass());
            }

            key = prefix + id;

            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json);
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
            String id = en.getKey();
            Object value = en.getValue();

            String key = "unknown";

            try {
                String prefix;

                if (value instanceof Auction || value instanceof AuctionEntity) {
                    prefix = AUCTIONS_PREFIX_DELIM;
                } else if (value instanceof Bid || value instanceof BidEntity) {
                    prefix = BIDS_PREFIX_DELIM;
                } else if (value instanceof User || value instanceof UserEntity) {
                    prefix = USERS_PREFIX_DELIM;
                } else {
                    throw new IllegalArgumentException("Unsupported type: " + value.getClass());
                }

                key = prefix + id;

                String json = objectMapper.writeValueAsString(value);
                redisMap.put(key, json);

            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to write Redis value for key: " + key, e);
            }
        }

        redisTemplate.opsForValue().multiSet(redisMap);
    }

    public void redisMultiSetWithVariants(Map<String, Pair<Object, Boolean>> map) {
        Map<String, Object> redisMap = new HashMap<>();

        for (Map.Entry<String, Pair<Object, Boolean>> en : map.entrySet()) {
            String id = en.getKey();
            Pair<Object, Boolean> pair = en.getValue();
            Object value = pair.getFirst();
            boolean flag = pair.getSecond();

            String key = "unknown";

            try {
                String prefix;

                if (value instanceof Auction || value instanceof AuctionEntity) {
                    prefix = AUCTIONS_PREFIX_DELIM;
                } else if (value instanceof Bid || value instanceof BidEntity) {
                    prefix = BIDS_PREFIX_DELIM;
                } else if (value instanceof User || value instanceof UserEntity) {
                    if (flag)
                        prefix = USERS_PREFIX_DELIM_EMAIL;
                    else
                        prefix = USERS_PREFIX_DELIM;
                } else {
                    throw new IllegalArgumentException("Unsupported type: " + value.getClass());
                }

                key = prefix + id;

                String json = objectMapper.writeValueAsString(value);
                redisMap.put(key, json);

            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to write Redis value for key: " + key, e);
            }
        }

        redisTemplate.opsForValue().multiSet(redisMap);
    }

    public <T> void redisDelete(String id, Class<T> type, boolean usesEmail) {
        String key = "null";
        try {
            String prefix = "";

            if (type == Auction.class || type == AuctionEntity.class) {
                prefix = AUCTIONS_PREFIX_DELIM;
            } else if (type == Bid.class || type == BidEntity.class) {
                prefix = BIDS_PREFIX_DELIM;
            } else if (type == User.class || type == UserEntity.class) {
                if (usesEmail)
                    prefix = USERS_PREFIX_DELIM_EMAIL;
                else
                    prefix = USERS_PREFIX_DELIM;
            } else {
                throw new IllegalArgumentException("Unsupported type: " + type.getClass());
            }

            key = prefix + id;

            redisTemplate.delete(key);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to read Redis value for key: " + key, e);
        }
    }

    public <T> void redisDelete(String id, Class<T> type) {
        redisDelete(id, type, false);
    }

    public void redisZAdd(String key, Object value, double score) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForZSet().add(key, json, score);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to write Redis ZSet value for key: " + key, e);
        }
    }
}
