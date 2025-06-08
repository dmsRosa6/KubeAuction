package com.dmsrosa.kubeaction.database.dao.repository;

import org.bson.types.ObjectId;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.dmsrosa.kubeaction.database.dao.entity.AuctionEntity;

public interface AuctionRepository extends PagingAndSortingRepository<AuctionEntity, ObjectId> {

}
