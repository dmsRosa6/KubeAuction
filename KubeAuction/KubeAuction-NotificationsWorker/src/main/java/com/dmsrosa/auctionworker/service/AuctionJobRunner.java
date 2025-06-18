package com.dmsrosa.auctionworker.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AuctionJobRunner {

    private static final String AUCTION_ZSET_KEY = "auction:expiration";

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Runs every 5 seconds
    @Scheduled(fixedRate = 5000)
    public void checkForExpiredAuctions() {
        long now = System.currentTimeMillis();

        // Find auctions with expiration timestamps <= now
        Set<ZSetOperations.TypedTuple<String>> expiredAuctions = redisTemplate.opsForZSet()
                .rangeByScoreWithScores(AUCTION_ZSET_KEY, 0, now);

        if (expiredAuctions == null || expiredAuctions.isEmpty())
            return;

        for (ZSetOperations.TypedTuple<String> auction : expiredAuctions) {
            String auctionId = auction.getValue();

            // Remove from zset
            redisTemplate.opsForZSet().remove(AUCTION_ZSET_KEY, auctionId);

            // Handle post-auction logic
            processAuctionEnd(auctionId);
        }
    }

    private void processAuctionEnd(String auctionId) {
        // TODO: Load winner from DB or Redis
        // TODO: Send notification (maybe publish to another queue or email service)
        // TODO: Update auction status in DB

        System.out.println("Auction " + auctionId + " ended. Processing winner notification.");
    }
}
