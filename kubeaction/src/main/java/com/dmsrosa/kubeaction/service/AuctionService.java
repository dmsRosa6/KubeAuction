package com.dmsrosa.kubeaction.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeaction.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeaction.database.dao.repository.AuctionRepository;

@Service
public class AuctionService {

    private final AuctionRepository auctionRepository;

    public AuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    public AuctionEntity createAuction(AuctionEntity auction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAuction'");
    }

    public Object getAuctionById(ObjectId id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAuctionById'");
    }

    public void softDeleteAuctionById(ObjectId id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'softDeleteAuctionById'");
    }

    public void markAuctionsOwnerDeletedByOwnerId(ObjectId ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'markAuctionsOwnerDeletedByOwnerId'");
    }
}
