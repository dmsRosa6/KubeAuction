package com.dmsrosa.kubeauction.dto.bid;

import org.bson.types.ObjectId;

import lombok.Data;

@Data
public class CreateBidDto {
    private ObjectId id;

    private ObjectId auctionId;

    private Integer value;

    private ObjectId userId;

    private Boolean isDeleted;

    private Boolean userDeleted;

    private Boolean auctionDeleted;
}
