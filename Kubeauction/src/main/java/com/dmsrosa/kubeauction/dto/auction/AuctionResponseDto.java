package com.dmsrosa.kubeauction.dto.auction;

import java.util.Date;
import java.util.UUID;

import org.bson.types.ObjectId;

import com.dmsrosa.kubeauction.database.dao.entity.AuctionEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuctionResponseDto {

    private ObjectId id;
    private String title;
    private String descr;
    private UUID imageId;
    private ObjectId ownerId;
    private Date endDate;
    private Integer minimumPrice;

    public static AuctionResponseDto fromAuctionEntity(AuctionEntity auction) {
        return AuctionResponseDto.builder().id(auction.getId()).title(auction.getTitle()).descr(auction.getDescr())
                .imageId(auction.getImageId()).ownerId(auction.getOwnerId()).endDate(auction.getEndDate())
                .minimumPrice(auction.getMinimumPrice()).build();
    }
}
