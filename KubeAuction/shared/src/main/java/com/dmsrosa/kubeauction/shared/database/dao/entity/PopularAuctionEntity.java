package com.dmsrosa.kubeauction.shared.database.dao.entity;

import java.io.Serializable;
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
@Document(collection = "popularAuctions")
public class PopularAuctionEntity implements Serializable {

    @Id
    private ObjectId id;

    private String title;

    private UUID imageId;

    @Builder.Default
    private Integer count = 0;

}
