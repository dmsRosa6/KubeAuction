package com.dmsrosa.notifications.service;

import java.util.Optional;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.shared.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.shared.database.dao.repository.BidRepository;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;
import com.dmsrosa.kubeauction.shared.redis.RedisRepository;

@Service
public class JobRunner {

    private static final int RATE = 3600; // 1 minute

    private final RedisRepository redis;

    private final BidRepository bidRepository;

    public JobRunner(RedisRepository redisRepository, BidRepository bidRepository) {
        this.redis = redisRepository;
        this.bidRepository = bidRepository;
    }

    // TODO I Could easily create a redis function that does the deletion on the
    // server side and returns a list/set with the due auctions
    @Scheduled(fixedRate = RATE)
    public void checkForExpiredAuctions() {

        var expiredAuctions = redis.getDueNotifications();

        if (expiredAuctions == null || expiredAuctions.isEmpty())
            return;

        for (TypedTuple<Object> a : expiredAuctions) {
            Auction auction = (Auction) a.getValue();

            redis.deleteFromNotifications(auction);
            processAuctionEnd(auction);
        }
    }

    // TODO This could be anything, for testing purposes i will use redis pub/sub to
    // send to a channel of the owner
    private void processAuctionEnd(Auction auction) {
        Optional<BidEntity> bid = bidRepository.findTopByAuctionIdOrderByValueDesc(auction.getId());

        if (bid.isEmpty())
            return;

        redis.publishToAuctionChannel(auction.getId().toString(), bid.get().getUserId());
    }
}
