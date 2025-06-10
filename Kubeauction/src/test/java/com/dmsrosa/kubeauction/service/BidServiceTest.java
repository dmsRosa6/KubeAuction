package com.dmsrosa.kubeauction.service;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.dmsrosa.kubeauction.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.database.dao.repository.BidRepository;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private BidService bidService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBid_shouldSaveBid() {
        BidEntity newBid = BidEntity.builder()
                .id(new ObjectId())
                .auctionId(new ObjectId())
                .userId(new ObjectId())
                .value(100)
                .build();

        when(bidRepository.save(newBid)).thenReturn(newBid);

        BidEntity result = bidService.createBid(newBid);

        assertEquals(newBid, result);
        verify(bidRepository).save(newBid);
    }

    @Test
    void deleteBid_shouldSetIsDeletedToTrue() {
        ObjectId bidId = new ObjectId();
        BidEntity existing = BidEntity.builder()
                .id(bidId)
                .isDeleted(false)
                .build();

        when(bidRepository.findById(bidId)).thenReturn(Optional.of(existing));
        when(bidRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        bidService.deleteBidById(bidId);

        assertTrue(existing.getIsDeleted());
        verify(bidRepository).save(existing);
    }

    @Test
    void deleteBid_shouldThrowIfNotFound() {
        ObjectId bidId = new ObjectId();
        when(bidRepository.findById(bidId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bidService.deleteBidById(bidId));
    }

    @Test
    void findBidById_shouldReturnBid() {
        ObjectId id = new ObjectId();
        BidEntity bid = BidEntity.builder().id(id).build();

        when(bidRepository.findById(id)).thenReturn(Optional.of(bid));

        BidEntity result = bidService.findBidById(id);

        assertEquals(bid, result);
    }

    @Test
    void findBidById_shouldThrowIfNotFound() {
        ObjectId id = new ObjectId();
        when(bidRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bidService.findBidById(id));
    }

    @Test
    void markUserDeletedByUserId_Success() {
        ObjectId userId = new ObjectId();

        BidEntity bid1 = BidEntity.builder()
                .id(new ObjectId())
                .userId(userId)
                .auctionId(new ObjectId())
                .value(100)
                .isDeleted(false)
                .userDeleted(false)
                .auctionDeleted(false)
                .build();

        BidEntity bid2 = BidEntity.builder()
                .id(new ObjectId())
                .userId(userId)
                .auctionId(new ObjectId())
                .value(200)
                .isDeleted(false)
                .userDeleted(false)
                .auctionDeleted(false)
                .build();

        BidEntity bid1After = bid1.builder().userDeleted(true).build();
        BidEntity bid2After = bid2.builder().userDeleted(true).build();

        when(bidRepository.findByUserId(userId)).thenReturn(List.of(bid1, bid2));
        when(bidRepository.saveAll(anyList())).thenReturn(List.of(bid1After, bid2After));

        bidService.markUserDeletedByUserId(userId);

        verify(bidRepository, times(1)).findByUserId(userId);
        verify(bidRepository, times(1)).saveAll(anyList());
    }

    @Test
    void markAuctionDeletedByAuctionId_Success() {
        ObjectId auctionId = new ObjectId();

        BidEntity bid1 = BidEntity.builder()
                .id(new ObjectId())
                .userId(new ObjectId())
                .auctionId(auctionId)
                .value(100)
                .isDeleted(false)
                .userDeleted(false)
                .auctionDeleted(false)
                .build();

        BidEntity bid2 = BidEntity.builder()
                .id(new ObjectId())
                .userId(new ObjectId())
                .auctionId(auctionId)
                .value(150)
                .isDeleted(false)
                .userDeleted(false)
                .auctionDeleted(false)
                .build();

        BidEntity bid1After = bid1.builder().auctionDeleted(true).build();
        BidEntity bid2After = bid2.builder().auctionDeleted(true).build();

        when(bidRepository.findByAuctionId(auctionId)).thenReturn(List.of(bid1, bid2));
        when(bidRepository.saveAll(anyList())).thenReturn(List.of(bid1After, bid2After));

        bidService.markAuctionDeletedByAuctionId(auctionId);

        verify(bidRepository, times(1)).findByAuctionId(auctionId);
        verify(bidRepository, times(1)).saveAll(anyList());
    }

}
