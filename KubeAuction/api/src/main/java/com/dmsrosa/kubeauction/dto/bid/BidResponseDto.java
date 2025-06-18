package com.dmsrosa.kubeauction.dto.bid;

import com.dmsrosa.kubeauction.shared.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.shared.database.domain.Bid;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonDeserialize(builder = BidResponseDto.BidResponseDtoBuilder.class)
@NoArgsConstructor
@AllArgsConstructor
public class BidResponseDto {
    private String id;

    private String auctionId;

    private Integer value;

    private String userId;

    private Boolean isDeleted;

    private Boolean userDeleted;

    private Boolean auctionDeleted;

    @JsonPOJOBuilder(withPrefix = "")
    public static class BidResponseDtoBuilder {
    }

    public static BidResponseDto fromBidEntity(Bid bidEntity) {
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
