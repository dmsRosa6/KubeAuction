package com.dmsrosa.kubeaction.database.dao.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.dmsrosa.kubeaction.database.dao.entity.BidEntity;

public interface BidRepository extends PagingAndSortingRepository<BidEntity, ObjectId> {
    List<BidEntity> findByAuction(ObjectId auctionId);
}
