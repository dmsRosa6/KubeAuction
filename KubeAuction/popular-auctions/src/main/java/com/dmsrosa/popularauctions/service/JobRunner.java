package com.dmsrosa.popularauctions.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.shared.database.dao.entity.PopularAuctionEntity;
import com.dmsrosa.kubeauction.shared.redis.RedisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JobRunner {

    private static final long FIVE_MIN = 1000 * 60 * 5;

    private final RedisRepository redis;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public JobRunner(RedisRepository redisRepository,
                     MongoTemplate mongoTemplate,
                     ObjectMapper objectMapper) {
        this.redis = redisRepository;
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    //TODO Pretty sure u can have MongoDB pipelines do a lot more than what i am doin
    @Scheduled(fixedRate = FIVE_MIN)
    public void getPopularAuctions() throws Exception {
        Date twoDaysAgo = Date.from(
            Instant.now().minus(2, ChronoUnit.DAYS)
        );

        MatchOperation match = Aggregation.match(
            Criteria.where("createdAt").gte(twoDaysAgo)
                    .and("isDeleted").is(false)
        );
        
        GroupOperation group = Aggregation.group("auctionId")
            .count().as("bidCount");
        
        Aggregation agg = Aggregation.newAggregation(match, group);
        
        List<Document> results = mongoTemplate
            .aggregate(agg, "bids", Document.class)
            .getMappedResults();

        mongoTemplate.dropCollection(PopularAuctionEntity.class);
        List<PopularAuctionEntity> snapshots = new ArrayList<>();
        
        for (Document doc : results) {
            ObjectId aid = doc.getObjectId("_id");
            int count = doc.getInteger("bidCount");

            AuctionEntity auction = mongoTemplate
                .findById(aid, AuctionEntity.class);

            if (auction == null
             || auction.getIsDeleted()
             || auction.getEndDate().before(new Date())) {
                continue;
            }
            
            snapshots.add(PopularAuctionEntity.builder()
                .id(aid)
                .title(auction.getTitle())
                .imageId(auction.getImageId())
                .count(count)
                .build());
        }


        if (!snapshots.isEmpty()) {
            String json = objectMapper.writeValueAsString(snapshots);

            redis.setPopular(snapshots);

            mongoTemplate.insertAll(snapshots);
        }
    }
}

