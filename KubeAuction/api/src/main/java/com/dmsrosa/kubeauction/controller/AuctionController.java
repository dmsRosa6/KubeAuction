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

import com.dmsrosa.kubeauction.dto.auction.AuctionResponseDto;
import com.dmsrosa.kubeauction.dto.auction.CreateAuctionDto;
import com.dmsrosa.kubeauction.dto.auction.UpdateAuctionDto;
import com.dmsrosa.kubeauction.exception.InvalidAuctionException;
import com.dmsrosa.kubeauction.service.AuctionService;
import com.dmsrosa.kubeauction.service.BidService;
import com.dmsrosa.kubeauction.service.ImageService;
import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;
    private final BidService bidService;
    private final ImageService imageService;

    public AuctionController(AuctionService auctionService, BidService bidService, ImageService imageService) {
        this.auctionService = auctionService;
        this.bidService = bidService;
        this.imageService = imageService;
    }

    private String toFilename(java.util.UUID uuid) {
        return uuid.toString() + ".jpeg";
    }

    @PostMapping
    public ResponseEntity<AuctionResponseDto> createAuction(@RequestBody CreateAuctionDto dto) {
        if (dto.getImageId() != null && !imageService.imageExists(toFilename(dto.getImageId()))) {
            throw new InvalidAuctionException("Image does not exist.");
        }

        Auction created = auctionService.createAuction(CreateAuctionDto.toAuction(dto));

        URI location = URI.create("/api/auctions/" + created.getId());

        return ResponseEntity.created(location).body(AuctionResponseDto.fromAuction(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponseDto> getAuctionById(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        Auction auction = auctionService.getAuctionById(oid, false);
        return ResponseEntity.ok(AuctionResponseDto.fromAuction(auction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuctionResponseDto> updateAuctionById(@PathVariable String id,
            @RequestBody UpdateAuctionDto dto) {

        if (dto.getImageId() != null && !imageService.imageExists(toFilename(dto.getImageId()))) {
            throw new InvalidAuctionException("Image does not exist.");
        }

        ObjectId oid = new ObjectId(id);
        Auction updated = auctionService.updateAuctionById(oid, UpdateAuctionDto.toAuction(dto));
        return ResponseEntity.ok(AuctionResponseDto.fromAuction(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuctionById(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        auctionService.softDeleteAuctionById(oid);
        bidService.markAuctionDeletedByAuctionId(oid);
        return ResponseEntity.noContent().build();
    }
}
