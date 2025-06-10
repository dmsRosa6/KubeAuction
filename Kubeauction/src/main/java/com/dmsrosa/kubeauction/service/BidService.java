package com.dmsrosa.kubeauction.service;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.config.RedisConfig;
import com.dmsrosa.kubeauction.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.database.dao.repository.BidRepository;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

@Service
public class BidService {

    private final BidRepository bidRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public BidService(BidRepository bidRepository, RedisTemplate<String, Object> redisTemplate) {
        this.bidRepository = bidRepository;
        this.redisTemplate = redisTemplate;
    }

    public void markUserDeletedByUserId(ObjectId userId) throws NotFoundException {
        List<BidEntity> list = bidRepository.findByUserId(userId);

        if (list.isEmpty()) {
            throw new NotFoundException("No bids found for user.id=%s", userId.toString());
        }

        list.forEach(bid -> {
            bid.setUserDeleted(true);
            redisTemplate.opsForValue().set(makeRedisKey(bid.getId().toString()), bid, RedisConfig.BIDS_DEFAULT_TTL);
        });
        bidRepository.saveAll(list);
    }

    public void markAuctionDeletedByAuctionId(ObjectId auctionId) throws NotFoundException {
        List<BidEntity> list = bidRepository.findByAuctionId(auctionId);
        if (list.isEmpty()) {
            throw new NotFoundException("No bids found for auction.id=%s", auctionId.toString());
        }

        list.forEach(bid -> {
            bid.setAuctionDeleted(true);
            redisTemplate.opsForValue().set(makeRedisKey(bid.getId().toString()), bid, RedisConfig.BIDS_DEFAULT_TTL);
        });
        bidRepository.saveAll(list);
    }

    @CachePut(value = "bidCache", key = "#result.id")
    public BidEntity createBid(BidEntity newBid) {
        return bidRepository.save(newBid);
    }

    public void deleteBidById(ObjectId bidId) throws NotFoundException {
        BidEntity bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new NotFoundException("Bid with id=%s not found", bidId.toString()));

        bid.setIsDeleted(true);
        bidRepository.save(bid);

        redisTemplate.opsForValue().set(makeRedisKey(bidId.toString()), bid, RedisConfig.BIDS_DEFAULT_TTL);
    }

    @Cacheable(value = "bidCache", key = "#id")
    public BidEntity findBidById(ObjectId id) throws NotFoundException {
        return bidRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bid with id=%s not found", id.toString()));
    }

    // private methods
    private String makeRedisKey(String id) {
        return RedisConfig.BIDS_PREXIX_DELIM + id;
    }
}
