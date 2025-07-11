package com.dmsrosa.kubeauction.shared.database.dao.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;

@Repository
public interface AuctionRepository extends MongoRepository<AuctionEntity, ObjectId> {

    List<AuctionEntity> findByOwnerId(ObjectId ownerId);

}
