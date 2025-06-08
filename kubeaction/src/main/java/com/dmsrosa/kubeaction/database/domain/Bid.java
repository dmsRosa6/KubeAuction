package com.dmsrosa.kubeaction.database.domain;

import org.bson.types.ObjectId;

import lombok.Data;

@Data
public class Bid {
    private ObjectId id;

    private ObjectId auctionId;

    private Integer value;

    private ObjectId userId;
}
