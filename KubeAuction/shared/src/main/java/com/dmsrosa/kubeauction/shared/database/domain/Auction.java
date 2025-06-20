package com.dmsrosa.kubeauction.shared.database.domain;

import java.util.Date;
import java.util.UUID;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {
    private ObjectId id;

    private String title;

    private String descr;

    private UUID imageId;

    private ObjectId ownerId;

    private Date endDate;

    private Integer minimumPrice;

    private boolean isDeleted;

    private boolean ownerDeleted;
}
