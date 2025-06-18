package com.dmsrosa.kubeauction.shared.database.dao.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
public class AuctionEntity implements Serializable {

    @Id
    private ObjectId id;

    private String title;

    private String descr;

    private UUID imageId;

    @Indexed
    private ObjectId ownerId;

    private Date endDate;

    private Integer minimumPrice;

    @Builder.Default
    private Boolean isDeleted = false;

    @Builder.Default
    private Boolean ownerDeleted = false;
}
