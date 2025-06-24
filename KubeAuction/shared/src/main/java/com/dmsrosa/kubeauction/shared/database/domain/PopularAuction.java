package com.dmsrosa.kubeauction.shared.database.domain;

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
public class PopularAuction {
    private ObjectId id;

    private String title;

    private UUID imageId;

    private int count;
}
