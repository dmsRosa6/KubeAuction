package com.dmsrosa.kubeauction.controller;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dmsrosa.kubeauction.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.dto.auction.CreateAuctionDto;
import com.dmsrosa.kubeauction.dto.auction.UpdateAuctionDto;
import com.dmsrosa.kubeauction.dto.auction.AuctionResponseDto;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(AuctionResponseDto.fromAuctionEntity(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponseDto> getAuctionById(@PathVariable ObjectId id) {
        AuctionEntity auction = auctionService.getAuctionById(id, false);
        return ResponseEntity.ok(AuctionResponseDto.fromAuctionEntity(auction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuctionResponseDto> updateAuctionById(@PathVariable ObjectId id,
            @RequestBody UpdateAuctionDto dto) {
        AuctionEntity updated = auctionService.updateAuctionById(id, UpdateAuctionDto.toAuctionEntity(dto));
        return ResponseEntity.ok(AuctionResponseDto.fromAuctionEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuctionById(@PathVariable ObjectId id) {
        auctionService.softDeleteAuctionById(id);
        bidService.markAuctionDeletedByAuctionId(id);
        return ResponseEntity.noContent().build();
    }
}
