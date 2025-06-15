package com.dmsrosa.kubeauction.service;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.config.MongoConfig;
import com.dmsrosa.kubeauction.config.RedisConfig;
import com.dmsrosa.kubeauction.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.database.dao.repository.BidRepository;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionService auctionService;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private String redisKey(ObjectId id) {
        return RedisConfig.BIDS_PREFIX_DELIM + id.toHexString();
    }

    public void markUserDeletedByUserId(ObjectId userId) {
        List<BidEntity> bids = bidRepository.findByUserId(userId);

        bids.forEach(bid -> {
            bid.setUserDeleted(true);
            redisTemplate.opsForValue().set(redisKey(bid.getId()), bid, RedisConfig.BIDS_DEFAULT_TTL);
        });
        bidRepository.saveAll(bids);
    }

    public void markAuctionDeletedByAuctionId(ObjectId auctionId) {
        List<BidEntity> bids = bidRepository.findByAuctionId(auctionId);

        bids.forEach(bid -> {
            bid.setAuctionDeleted(true);
            redisTemplate.opsForValue().set(redisKey(bid.getId()), bid, RedisConfig.BIDS_DEFAULT_TTL);
        });
        bidRepository.saveAll(bids);
    }

    public BidEntity createBid(BidEntity newBid) {
        // validate references
        userService.getUserById(newBid.getUserId(), false);
        auctionService.getAuctionById(newBid.getAuctionId(), false);
        BidEntity saved = bidRepository.save(newBid);
        redisTemplate.opsForValue().set(redisKey(saved.getId()), saved, RedisConfig.BIDS_DEFAULT_TTL);
        return saved;
    }

    public void deleteBidById(ObjectId bidId) {
        BidEntity bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new NotFoundException("Bid with id=%s not found", bidId.toHexString()));
        bid.setIsDeleted(true);
        BidEntity saved = bidRepository.save(bid);
        redisTemplate.opsForValue().set(redisKey(bidId), saved, RedisConfig.BIDS_DEFAULT_TTL);
    }

    public BidEntity findBidById(ObjectId id) {
        String key = redisKey(id);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof BidEntity) {
            return (BidEntity) cached;
        }
        BidEntity bid = bidRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bid with id=%s not found", id.toHexString()));
        redisTemplate.opsForValue().set(key, bid, RedisConfig.BIDS_DEFAULT_TTL);
        return bid;
    }

    public List<BidEntity> getAuctionBids(ObjectId auctionId) {
        auctionService.getAuctionById(auctionId, false);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("auctionDeleted").is(false)
                                .and("auctionId").is(auctionId)));
        AggregationResults<BidEntity> results = mongoTemplate.aggregate(agg, MongoConfig.BIDS_DB,
                BidEntity.class);
        return results.getMappedResults();
    }

    public List<BidEntity> getUserBids(ObjectId userId) {
        userService.getUserById(userId, false);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("userDeleted").is(false)
                                .and("userId").is(userId)));
        AggregationResults<BidEntity> results = mongoTemplate.aggregate(agg, MongoConfig.BIDS_DB,
                BidEntity.class);
        return results.getMappedResults();
    }
}
