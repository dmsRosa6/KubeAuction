package com.dmsrosa.kubeauction.service;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dmsrosa.kubeauction.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.database.dao.repository.AuctionRepository;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

@Service
public class AuctionService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private final AuctionRepository auctionRepository;

    public AuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    public AuctionEntity createAuction(AuctionEntity auction) {

        AuctionEntity a = auctionRepository.save(auction);

        return a;
    }

    public AuctionEntity getAuctionById(ObjectId id, boolean getDeleted) {
        Optional<AuctionEntity> a = auctionRepository.findById(id);

        if (a.isEmpty() || a.get().getIsDeleted())
            throw new NotFoundException("Auction not found.id=%s", id.toString());

        return a.get();
    }

    public void softDeleteAuctionById(ObjectId id) {
        AuctionEntity a = this.getAuctionById(id, false);

        a.setIsDeleted(true);

        this.auctionRepository.save(a);
    }

    public void markOwnerDeletedByOwnerId(ObjectId ownerId) {
        Query query = new Query(Criteria.where("ownerId").is(ownerId));
        Update update = new Update().set("ownerDeleted", true);
        mongoTemplate.updateMulti(query, update, AuctionEntity.class);
    }
}
