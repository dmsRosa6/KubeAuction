package com.dmsrosa.kubeauction.database.dao.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.dmsrosa.kubeauction.database.dao.entity.AuctionEntity;

public interface AuctionRepository extends MongoRepository<AuctionEntity, ObjectId> {

    List<AuctionEntity> findByOwnerId(ObjectId ownerId);

}
