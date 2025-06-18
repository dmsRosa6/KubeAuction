package com.dmsrosa.kubeauction.shared.database.domain;

import org.bson.types.ObjectId;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Bid {
    private ObjectId id;

    private ObjectId auctionId;

    private Integer value;

    private ObjectId userId;

    private Boolean isDeleted;

    private Boolean userDeleted;

    private Boolean auctionDeleted;
}
