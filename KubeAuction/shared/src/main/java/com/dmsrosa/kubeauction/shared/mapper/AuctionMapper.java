package com.dmsrosa.kubeauction.shared.mapper;

import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;

public class AuctionMapper {

    public static Auction toDomain(AuctionEntity entity) {
        Auction auction = new Auction();
        auction.setId(entity.getId());
        auction.setTitle(entity.getTitle());
        auction.setDescr(entity.getDescr());
        auction.setImageId(entity.getImageId());
        auction.setOwnerId(entity.getOwnerId());
        auction.setEndDate(entity.getEndDate());
        auction.setMinimumPrice(entity.getMinimumPrice());
        auction.setDeleted(entity.getIsDeleted());
        auction.setOwnerDeleted(entity.getOwnerDeleted());

        return auction;
    }

    public static AuctionEntity toEntity(Auction domain) {
        return AuctionEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .descr(domain.getDescr())
                .imageId(domain.getImageId())
                .ownerId(domain.getOwnerId())
                .endDate(domain.getEndDate())
                .minimumPrice(domain.getMinimumPrice())
                .isDeleted(domain.isDeleted())
                .ownerDeleted(domain.isOwnerDeleted())
                .build();
    }
}
