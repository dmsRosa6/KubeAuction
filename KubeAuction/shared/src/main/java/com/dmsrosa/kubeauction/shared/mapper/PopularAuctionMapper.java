package com.dmsrosa.kubeauction.shared.mapper;

import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.shared.database.dao.entity.PopularAuctionEntity;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;
import com.dmsrosa.kubeauction.shared.database.domain.PopularAuction;

public class PopularAuctionMapper {
    public static PopularAuction toDomain(PopularAuctionEntity entity) {
        PopularAuction auction = new PopularAuction();
        auction.setId(entity.getId());
        auction.setTitle(entity.getTitle());
        auction.setCount(entity.getCount());

        return auction;
    }

    public static PopularAuctionEntity toEntity(PopularAuction domain) {
        return PopularAuctionEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .count(domain.getCount())
                .build();
    }
}
