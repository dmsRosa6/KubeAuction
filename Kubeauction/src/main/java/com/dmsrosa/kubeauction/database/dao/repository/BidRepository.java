package com.dmsrosa.kubeauction.database.dao.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.dmsrosa.kubeauction.database.dao.entity.BidEntity;

public interface BidRepository extends MongoRepository<BidEntity, ObjectId> {
    List<BidEntity> findByAuctionId(ObjectId auctionId);

    List<BidEntity> findByUserId(ObjectId userId);

    List<BidEntity> findByUserIdAndAuctionId(ObjectId userId, ObjectId auctionId);
}
