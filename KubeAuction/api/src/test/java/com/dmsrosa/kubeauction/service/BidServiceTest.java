package com.dmsrosa.kubeauction.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.dmsrosa.kubeauction.exception.NotFoundException;
import com.dmsrosa.kubeauction.shared.database.dao.entity.BidEntity;
import com.dmsrosa.kubeauction.shared.database.dao.repository.BidRepository;
import com.dmsrosa.kubeauction.shared.database.domain.Bid;
import com.dmsrosa.kubeauction.shared.mapper.BidMapper;
import com.dmsrosa.kubeauction.shared.redis.RedisRepository;

class BidServiceTest {

    @Mock
    private BidRepository bidRepository;
    @Mock
    private UserService userService;
    @Mock
    private AuctionService auctionService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private BidService bidService;

    private ObjectId bidId;
    private Bid domainBid;
    private BidEntity entityBid;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bidId = new ObjectId();

        domainBid = Bid.builder()
                .id(bidId)
                .auctionId(new ObjectId())
                .userId(new ObjectId())
                .value(123)
                .isDeleted(false)
                .userDeleted(false)
                .auctionDeleted(false)
                .build();
        entityBid = BidMapper.toEntity(domainBid);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void createBid_shouldSaveAndReturnDomainBid() {
        Bid newDomain = Bid.builder()
                .id(new ObjectId())
                .auctionId(new ObjectId())
                .userId(new ObjectId())
                .value(100)
                .build();
        BidEntity newEntity = BidMapper.toEntity(newDomain);

        when(bidRepository.save(any(BidEntity.class))).thenReturn(newEntity);
        when(userService.getUserById(any(), anyBoolean())).thenReturn(null);
        when(auctionService.getAuctionById(any(), anyBoolean())).thenReturn(null);

        Bid result = bidService.createBid(newDomain);

        assertThat(result).isEqualTo(newDomain);
        verify(bidRepository, times(1)).save(any(BidEntity.class));
    }

    @Test
    void deleteBidById_shouldMarkDeletedAndSave() {
        BidEntity existing = BidEntity.builder().isDeleted(false).build();
        when(bidRepository.findById(bidId)).thenReturn(Optional.of(existing));
        when(bidRepository.save(any(BidEntity.class))).thenAnswer(i -> i.getArgument(0));

        bidService.deleteBidById(bidId);

        assertThat(existing.getIsDeleted()).isTrue();
        verify(bidRepository).save(existing);
    }

    @Test
    void deleteBidById_notFound_throws() {
        when(bidRepository.findById(bidId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bidService.deleteBidById(bidId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bid not found");
    }

    @Test
    void findBidById_cacheHit_returnsFromCache() {
        when(valueOps.get(RedisRepository.BIDS_PREFIX_DELIM + bidId)).thenReturn(domainBid);

        Bid result = bidService.findBidById(bidId, false);

        assertThat(result).isSameAs(domainBid);
        verify(bidRepository, never()).findById(any());
    }

    @Test
    void findBidById_cacheMiss_thenDbAndCache() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(bidRepository.findById(bidId)).thenReturn(Optional.of(entityBid));

        Bid result = bidService.findBidById(bidId, false);

        assertThat(result).isEqualTo(BidMapper.toDomain(entityBid));
        verify(bidRepository).findById(bidId);
        verify(valueOps).set(
                RedisRepository.BIDS_PREFIX_DELIM + bidId,
                BidMapper.toDomain(entityBid));
    }

    @Test
    void findBidById_notFoundAfterCacheMiss_throws() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(bidRepository.findById(bidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bidService.findBidById(bidId, false))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bid with id=");

        verify(bidRepository).findById(bidId);
    }

    @Test
    void markUserDeletedByUserId_succeeds() {
        ObjectId userId = new ObjectId();
        BidEntity b1 = BidEntity.builder().userId(userId).build();
        BidEntity b2 = BidEntity.builder().userId(userId).build();

        BidEntity b1After = BidEntity.builder().userDeleted(true).build();
        BidEntity b2After = BidEntity.builder().userDeleted(true).build();

        when(bidRepository.findByUserId(userId)).thenReturn(List.of(b1, b2));
        when(bidRepository.saveAll(anyList())).thenReturn(List.of(b1After, b2After));

        bidService.markUserDeletedByUserId(userId);

        verify(bidRepository).findByUserId(userId);
        verify(bidRepository).saveAll(anyList());
    }

    @Test
    void markAuctionDeletedByAuctionId_succeeds() {
        ObjectId auctionId = new ObjectId();
        BidEntity b1 = BidEntity.builder().auctionId(auctionId).build();
        BidEntity b2 = BidEntity.builder().auctionId(auctionId).build();

        BidEntity b1After = BidEntity.builder().auctionDeleted(true).build();
        BidEntity b2After = BidEntity.builder().auctionDeleted(true).build();

        when(bidRepository.findByAuctionId(auctionId)).thenReturn(List.of(b1, b2));
        when(bidRepository.saveAll(anyList())).thenReturn(List.of(b1After, b2After));

        bidService.markAuctionDeletedByAuctionId(auctionId);

        verify(bidRepository).findByAuctionId(auctionId);
        verify(bidRepository).saveAll(anyList());
    }
}
