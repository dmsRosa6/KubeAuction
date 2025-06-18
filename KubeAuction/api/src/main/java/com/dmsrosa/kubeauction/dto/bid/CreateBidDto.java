package com.dmsrosa.kubeauction.dto.bid;

import org.bson.types.ObjectId;

import com.dmsrosa.kubeauction.shared.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.shared.database.domain.Bid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBidDto {

    private String auctionId;

    private Integer value;

    private String userId;

    public static Bid toBidEntity(CreateBidDto bidDto) {
        return Bid.builder()
                .auctionId(new ObjectId(bidDto.getAuctionId()))
                .value(bidDto.getValue())
                .userId(new ObjectId(bidDto.getUserId()))
                .build();
    }
}
