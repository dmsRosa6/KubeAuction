package com.dmsrosa.kubeauction.controller;

import java.net.URI;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmsrosa.kubeauction.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.dto.bid.BidResponseDto;
import com.dmsrosa.kubeauction.dto.bid.CreateBidDto;
import com.dmsrosa.kubeauction.service.BidService;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

@RestController
@RequestMapping("/api/bids")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping
    public ResponseEntity<BidResponseDto> createBid(@RequestBody CreateBidDto newBid) {
        BidEntity created = bidService.createBid(CreateBidDto.toBidEntity(newBid));
        URI location = URI.create("/api/bids/" + created.getId());

        return ResponseEntity.created(location).body(BidResponseDto.fromBidEntity(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBidById(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        try {
            BidEntity bid = bidService.findBidById(oid);
            return ResponseEntity.ok(bid);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBid(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        bidService.deleteBidById(oid);
        return ResponseEntity.noContent().build();
    }
}
