package com.dmsrosa.kubeauction.shared.redis.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class AuctionTask implements Serializable {
    private String auctionId;
    private String userId;
    private String userEmail;
    private String bidId;
}