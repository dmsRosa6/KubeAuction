package com.dmsrosa.kubeauction.controller;

import java.net.URI;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmsrosa.kubeauction.dto.bid.BidResponseDto;
import com.dmsrosa.kubeauction.dto.bid.CreateBidDto;
import com.dmsrosa.kubeauction.service.BidService;
import com.dmsrosa.kubeauction.shared.database.domain.Bid;

@RestController
@RequestMapping("/api/bids")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping
    public ResponseEntity<BidResponseDto> createBid(@RequestBody CreateBidDto newBid) {
        Bid created = bidService.createBid(CreateBidDto.toBidEntity(newBid));
        URI location = URI.create("/api/bids/" + created.getId());

        return ResponseEntity.created(location).body(BidResponseDto.fromBidEntity(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BidResponseDto> getBidById(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        Bid r = bidService.findBidById(oid, false);
        BidResponseDto bid = BidResponseDto.fromBidEntity(r);
        return ResponseEntity.ok(bid);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBid(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        bidService.deleteBidById(oid);
        return ResponseEntity.noContent().build();
    }
}
