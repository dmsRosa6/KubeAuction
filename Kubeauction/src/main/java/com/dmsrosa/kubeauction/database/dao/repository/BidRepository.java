package com.dmsrosa.kubeauction.database.dao.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.dmsrosa.kubeauction.database.dao.entity.BidEntity;

public interface BidRepository extends PagingAndSortingRepository<BidEntity, ObjectId> {
    List<BidEntity> findByAuction(ObjectId auctionId);
}
