package com.dmsrosa.kubeauction.dto.auction;

import java.util.Date;
import java.util.UUID;

import org.bson.types.ObjectId;

import com.dmsrosa.kubeauction.database.dao.entity.AuctionEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateAuctionDto {

    private String title;
    private String descr;
    private UUID imageId;
    private ObjectId ownerId;
    private Date endDate;
    private Integer minimumPrice;

    public static AuctionEntity toAuctionEntity(CreateAuctionDto dto) {
        return AuctionEntity.builder()
                .title(dto.getTitle())
                .descr(dto.getDescr())
                .imageId(dto.getImageId())
                .ownerId(dto.getOwnerId())
                .endDate(dto.getEndDate())
                .minimumPrice(dto.getMinimumPrice())
                .build();
    }
}
