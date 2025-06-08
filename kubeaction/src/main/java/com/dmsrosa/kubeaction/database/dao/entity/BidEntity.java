package com.dmsrosa.kubeaction.database.dao.entity;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "bids")
public class BidEntity {

    @Id
    private ObjectId id;

    private ObjectId auctionId;

    private Integer value;

    private ObjectId userId;
}
