package com.dmsrosa.kubeauction.service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Fallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.config.MongoConfig;
import com.dmsrosa.kubeauction.exception.InvalidBidException;
import com.dmsrosa.kubeauction.exception.NotFoundException;
import com.dmsrosa.kubeauction.shared.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.shared.database.dao.repository.BidRepository;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;
import com.dmsrosa.kubeauction.shared.database.domain.Bid;
import com.dmsrosa.kubeauction.shared.mapper.BidMapper;
import com.dmsrosa.kubeauction.shared.redis.RedisRepository;

@Service
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionService auctionService;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final RedisRepository redis;

    public BidService(BidRepository bidRepository, AuctionService auctionService, UserService userService,
            MongoTemplate mongoTemplate, RedisRepository redisRepository) {
        this.bidRepository = bidRepository;
        this.auctionService = auctionService;
        this.userService = userService;
        this.mongoTemplate = mongoTemplate;
        this.redis = redisRepository;
    }

    public void markUserDeletedByUserId(ObjectId userId) {
        List<BidEntity> bids = bidRepository.findByUserId(userId);

        bids.forEach(bid -> {
            bid.setUserDeleted(true);
            Bid b = BidMapper.toDomain(bid);
            redis.redisSet(b.getId().toString(), b);
        });
        bidRepository.saveAll(bids);
    }

    public void markAuctionDeletedByAuctionId(ObjectId auctionId) {
        List<BidEntity> bids = bidRepository.findByAuctionId(auctionId);

        bids.forEach(bid -> {
            bid.setAuctionDeleted(true);
            Bid b = BidMapper.toDomain(bid);
            redis.redisSet(b.getId().toString(), b);
        });
        bidRepository.saveAll(bids);
    }

    public Bid createBid(Bid newBid) {
        userService.getUserById(newBid.getUserId(), false);

        Auction auction = auctionService.getAuctionById(newBid.getAuctionId(), false);

        if (auction.getEndDate().before(Date.from(Instant.now()))) {
            throw new InvalidBidException("Auction is closed or deleted.");
        }

        if (newBid.getValue() < auction.getMinimumPrice()) {
            throw new InvalidBidException("Bid is below the minimum price.");
        }

        Optional<BidEntity> highestBidOpt = bidRepository.findTopByAuctionIdOrderByValueDesc(newBid.getAuctionId());

        if (highestBidOpt.isPresent()) {
            int highest = highestBidOpt.get().getValue();
            if (newBid.getValue() <= highest) {
                throw new InvalidBidException("You must outbid the current highest bid of " + highest + ".");
            }
        }

        BidEntity b = bidRepository.save(BidMapper.toEntity(newBid));
        Bid saved = BidMapper.toDomain(b);

        redis.redisSet(saved.getId().toString(), saved);
        return saved;
    }

    public void deleteBidById(ObjectId bidId) {
        BidEntity bid = BidMapper.toEntity(findBidById(bidId, false));

        bid.setIsDeleted(true);
        bidRepository.save(bid);

        redis.redisDelete(bidId.toString(), Bid.class);
    }

    public Bid findBidById(ObjectId id, boolean getIsDeleted) {
        Bid cached = redis.redisGet(id.toString(), Bid.class);

        if (getIsDeleted || !cached.getIsDeleted())
            return cached;

        Optional<BidEntity> o = bidRepository.findById(id);

        if (o.isEmpty())
            throw new NotFoundException("Bid with id=%s not found", id.toString());

        Bid bid = BidMapper.toDomain(o.get());

        if (getIsDeleted || !bid.getIsDeleted())
            throw new NotFoundException("Bid with id=%s not found", id.toString());

        redis.redisSet(bid.getId().toString(), bid);

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
