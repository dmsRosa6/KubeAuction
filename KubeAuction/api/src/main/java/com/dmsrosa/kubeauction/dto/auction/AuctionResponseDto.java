package com.dmsrosa.kubeauction.dto.auction;

import java.util.Date;
import java.util.UUID;

import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonDeserialize(builder = AuctionResponseDto.AuctionResponseDtoBuilder.class)
@NoArgsConstructor
@AllArgsConstructor
public class AuctionResponseDto {

    private String id;

    private String title;

    private String descr;

    private UUID imageId;

    private String ownerId;

    private Date endDate;

    private Integer minimumPrice;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AuctionResponseDtoBuilder {
    }

    public static AuctionResponseDto fromAuction(Auction auction) {
        return AuctionResponseDto.builder().id(auction.getId().toString())
                .title(auction.getTitle())
                .descr(auction.getDescr())
                .imageId(auction.getImageId()).ownerId(auction.getOwnerId().toString()).endDate(auction.getEndDate())
                .minimumPrice(auction.getMinimumPrice()).build();
    }
}
