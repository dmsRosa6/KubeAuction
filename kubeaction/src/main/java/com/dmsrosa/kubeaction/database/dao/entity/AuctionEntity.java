package com.dmsrosa.kubeaction.database.dao.entity;

import java.util.Date;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "auctions")
public class AuctionEntity {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_OWNER_DELETED = "OWNER_DELETED";
    public static final String STATUS_ENDED = "ENDED";

    @Id
    private ObjectId id;

    private String title;

    private String descr;

    private UUID imageId;

    private ObjectId ownerId;

    private Date endDate;

    private Double minimumPrice;

    private String status;

    @Builder.Default
    private boolean isDeleted = false;

    @Builder.Default
    private boolean ownerDeleted = false;
}
