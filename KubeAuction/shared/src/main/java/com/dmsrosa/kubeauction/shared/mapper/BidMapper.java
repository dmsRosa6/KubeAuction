package com.dmsrosa.kubeauction.shared.mapper;

import com.dmsrosa.kubeauction.shared.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.shared.database.domain.Bid;

public class BidMapper {

    public static Bid toDomain(BidEntity entity) {
        Bid bid = new Bid();
        bid.setId(entity.getId());
        bid.setAuctionId(entity.getAuctionId());
        bid.setUserId(entity.getUserId());
        bid.setValue(entity.getValue());
        bid.setIsDeleted(entity.getIsDeleted());
        bid.setUserDeleted(entity.getUserDeleted());
        bid.setAuctionDeleted(entity.getAuctionDeleted());
        return bid;
    }

    public static BidEntity toEntity(Bid domain) {
        return BidEntity.builder()
                .id(domain.getId())
                .auctionId(domain.getAuctionId())
                .userId(domain.getUserId())
                .value(domain.getValue())
                .isDeleted(domain.getIsDeleted())
                .userDeleted(domain.getUserDeleted())
                .auctionDeleted(domain.getAuctionDeleted())
                .build();
    }
}
