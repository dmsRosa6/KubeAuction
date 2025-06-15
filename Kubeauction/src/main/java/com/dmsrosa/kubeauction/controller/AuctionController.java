package com.dmsrosa.kubeauction.controller;

import java.net.URI;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmsrosa.kubeauction.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.dto.auction.AuctionResponseDto;
import com.dmsrosa.kubeauction.dto.auction.CreateAuctionDto;
import com.dmsrosa.kubeauction.dto.auction.UpdateAuctionDto;
import com.dmsrosa.kubeauction.service.AuctionService;
import com.dmsrosa.kubeauction.service.BidService;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;
    private final BidService bidService;

    public AuctionController(AuctionService auctionService, BidService bidService) {
        this.auctionService = auctionService;
        this.bidService = bidService;
    }

    @PostMapping
    public ResponseEntity<AuctionResponseDto> createAuction(@RequestBody CreateAuctionDto dto) {
        AuctionEntity created = auctionService.createAuction(CreateAuctionDto.toAuctionEntity(dto));
        URI location = URI.create("/api/auctions/" + created.getId());

        return ResponseEntity.created(location).body(AuctionResponseDto.fromAuctionEntity(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponseDto> getAuctionById(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        AuctionEntity auction = auctionService.getAuctionById(oid, false);
        return ResponseEntity.ok(AuctionResponseDto.fromAuctionEntity(auction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuctionResponseDto> updateAuctionById(@PathVariable String id,
            @RequestBody UpdateAuctionDto dto) {
        ObjectId oid = new ObjectId(id);
        AuctionEntity updated = auctionService.updateAuctionById(oid, UpdateAuctionDto.toAuctionEntity(dto));
        return ResponseEntity.ok(AuctionResponseDto.fromAuctionEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuctionById(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        auctionService.softDeleteAuctionById(oid);
        bidService.markAuctionDeletedByAuctionId(oid);
        return ResponseEntity.noContent().build();
    }
}
