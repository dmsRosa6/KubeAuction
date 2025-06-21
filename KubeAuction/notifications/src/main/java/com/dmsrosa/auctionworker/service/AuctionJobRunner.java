package com.dmsrosa.auctionworker.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.shared.database.domain.Auction;
import com.dmsrosa.kubeauction.shared.redis.RedisRepository;

@Service
public class AuctionJobRunner {

    private static final int RATE = 3600; // 1 minute

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Scheduled(fixedRate = RATE)
    public void checkForExpiredAuctions() {
        long now = System.currentTimeMillis();

        Set<ZSetOperations.TypedTuple<String>> expiredAuctions = redisTemplate.opsForZSet()
                .rangeByScoreWithScores(RedisRepository.AUCTIONS_NOTIFICATIONS_KEY, 0, now);

        if (expiredAuctions == null || expiredAuctions.isEmpty())
            return;

        for (ZSetOperations.TypedTuple<String> a : expiredAuctions) {
            // TODO this cast is possibly the worse way of doing this
            Auction auction = (Auction) a;

            redisTemplate.opsForZSet().remove(RedisRepository.AUCTIONS_NOTIFICATIONS_KEY, auction);

            processAuctionEnd(auction.getId().toString());
        }
    }

    // TODO This could be anything, for testing purposes i will use redis pub/sub
    private void processAuctionEnd(String auctionId) {
        System.out.println("Auction " + auctionId + " ended. Processing winner notification.");
    }
}
