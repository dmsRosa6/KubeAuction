package com.dmsrosa.kubeauction.dto.bid;

import org.bson.types.ObjectId;

import com.dmsrosa.kubeauction.database.dao.entity.BidEntity;

import lombok.Data;

@Data
public class CreateBidDto {

    private String auctionId;

    private Integer value;

    private String userId;

    private Boolean isDeleted;

    private Boolean userDeleted;

    private Boolean auctionDeleted;

    public static BidEntity toBidEntity(CreateBidDto bidDto) {
        return BidEntity.builder()
                .auctionId(new ObjectId(bidDto.getAuctionId()))
                .value(bidDto.getValue())
                .userId(new ObjectId(bidDto.getUserId()))
                .isDeleted(bidDto.getIsDeleted())
                .userDeleted(bidDto.userDeleted)
                .auctionDeleted(bidDto.auctionDeleted)
                .build();
    }
}
