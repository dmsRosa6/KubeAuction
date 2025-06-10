package com.dmsrosa.kubeauction.dto.auction;

import java.util.UUID;

import com.dmsrosa.kubeauction.database.dao.entity.AuctionEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateAuctionDto {
    private String title;
    private String descr;
    private UUID imageId;

    public static AuctionEntity toAuctionEntity(UpdateAuctionDto dto) {
        return AuctionEntity.builder()
                .title(dto.getTitle())
                .descr(dto.getDescr())
                .imageId(dto.getImageId())
                .build();
    }
}
