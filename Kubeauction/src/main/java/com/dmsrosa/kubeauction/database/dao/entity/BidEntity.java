package com.dmsrosa.kubeauction.database.dao.entity;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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

    @Indexed
    private ObjectId auctionId;

    @Indexed
    private ObjectId userId;

    private int value;

    private Date createdAt;

    @Builder.Default
    private Boolean isDeleted = false;

    @Builder.Default
    private Boolean userDeleted = false;

    @Builder.Default
    private Boolean auctionDeleted = false;
}
