package com.dmsrosa.kubeauction.dto.bid;

import com.dmsrosa.kubeauction.database.dao.entity.BidEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BidResponseDto {
    private String id;

    private String auctionId;

    private Integer value;

    private String userId;

    private Boolean isDeleted;

    private Boolean userDeleted;

    private Boolean auctionDeleted;

    public static BidResponseDto fromBidEntity(BidEntity bidEntity) {
        return BidResponseDto.builder()
                .id(bidEntity.getId().toString())
                .auctionId(bidEntity.getAuctionId().toString())
                .value(bidEntity.getValue())
                .userId(bidEntity.getUserId().toString())
                .isDeleted(bidEntity.getIsDeleted())
                .userDeleted(bidEntity.getUserDeleted())
                .auctionDeleted(bidEntity.getAuctionDeleted())
                .build();
    }
}
