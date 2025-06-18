package com.dmsrosa.kubeauction.dto.auction;

import java.util.UUID;

import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAuctionDto {
    private String title;
    private String descr;
    private UUID imageId;

    public static Auction toAuction(UpdateAuctionDto dto) {
        return Auction.builder()
                .title(dto.getTitle())
                .descr(dto.getDescr())
                .imageId(dto.getImageId())
                .build();
    }
}
