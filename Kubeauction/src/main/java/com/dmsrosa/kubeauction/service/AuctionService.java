package com.dmsrosa.kubeauction.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.config.RedisConfig;
import com.dmsrosa.kubeauction.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.database.dao.repository.AuctionRepository;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

@Service
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public AuctionService(AuctionRepository auctionRepository, RedisTemplate<String, Object> redisTemplate) {
        this.auctionRepository = auctionRepository;
        this.redisTemplate = redisTemplate;
    }

    @CachePut(value = "auctionCache", key = "#result.id")
    public AuctionEntity createAuction(AuctionEntity auction) {
        return auctionRepository.save(auction);
    }

    @Cacheable(value = "auctionCache", key = "#id")
    public AuctionEntity getAuctionById(ObjectId id, boolean getDeleted) throws NotFoundException {
        Optional<AuctionEntity> a = auctionRepository.findById(id);
        if (a.isEmpty() || (!getDeleted && a.get().getIsDeleted()))
            throw new NotFoundException("Auction not found.id=%s", id.toString());
        return a.get();
    }

    // TODO CACHE EVICTION
    public AuctionEntity updateAuctionById(ObjectId id, AuctionEntity update) {
        AuctionEntity auction = this.getAuctionById(id, false);

        updateAuction(auction, update);
        AuctionEntity savedUser = this.auctionRepository.save(auction);

        return savedUser;
    }

    @CacheEvict(value = "auctionCache", key = "#id")
    public void softDeleteAuctionById(ObjectId id) throws NotFoundException {
        AuctionEntity a = this.getAuctionById(id, false);
        a.setIsDeleted(true);
        this.auctionRepository.save(a);
    }

    // TODO EVICTION
    public void markOwnerDeletedByOwnerId(ObjectId ownerId) throws NotFoundException {
        List<AuctionEntity> list = auctionRepository.findByOwnerId(ownerId);
        if (list.isEmpty()) {
            throw new NotFoundException("No auctions found for user.id=%s", ownerId.toString());
        }

        Map<String, Object> map = new HashMap<>();

        list.forEach(auction -> {
            auction.setOwnerDeleted(true);
            map.put(auction.getId().toString(), auction);
            redisTemplate.opsForValue().set(
                    makeRedisKey(auction.getId().toString()),
                    auction);
        });

        redisTemplate.opsForValue().multiSet(map);
        auctionRepository.saveAll(list);
    }

    // private methods
    private String makeRedisKey(String id) {
        return RedisConfig.AUCTIONS_PREFIX_DELIM + id;
    }

    // TODO
    private void updateAuction(AuctionEntity auction, AuctionEntity updates) {

    }
}