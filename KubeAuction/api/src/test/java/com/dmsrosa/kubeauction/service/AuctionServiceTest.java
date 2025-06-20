package com.dmsrosa.kubeauction.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.dmsrosa.kubeauction.exception.NotFoundException;
import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.shared.database.dao.repository.AuctionRepository;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;
import com.dmsrosa.kubeauction.shared.mapper.AuctionMapper;
import com.dmsrosa.kubeauction.shared.redis.RedisRepository;

// [Package and imports stay the same]

public class AuctionServiceTest {

        @Mock
        private AuctionRepository auctionRepository;

        @Mock
        private UserService userService;

        @Mock
        private RedisTemplate<String, Object> redisTemplate;

        @Mock
        private ValueOperations<String, Object> valueOperations;

        @InjectMocks
        private AuctionService auctionService;

        private ObjectId id;
        private Auction auction;
        private AuctionEntity auctionEntity;

        @BeforeEach
        void setup() {
                MockitoAnnotations.openMocks(this);
                id = new ObjectId();
                auction = Auction.builder()
                                .id(id)
                                .ownerId(new ObjectId())
                                .title("Original")
                                .descr("Desc")
                                .imageId(UUID.randomUUID())
                                .endDate(new Date())
                                .minimumPrice(10)
                                .isDeleted(false)
                                .ownerDeleted(false)
                                .build();
                auctionEntity = AuctionMapper.toEntity(auction);

                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void createAuction_Success() {
                when(auctionRepository.save(any(AuctionEntity.class))).thenReturn(auctionEntity);
                when(userService.getUserById(any(), anyBoolean())).thenReturn(null);

                Auction result = auctionService.createAuction(auction);

                assertThat(result).isNotNull();
                assertThat(result.getTitle()).isEqualTo("Original");
                assertThat(result.isDeleted()).isFalse();
                assertThat(result.isOwnerDeleted()).isFalse();

                verify(auctionRepository, times(1)).save(any(AuctionEntity.class));
        }

        @Test
        void getAuctionById_NotFound() {
                when(auctionRepository.findById(id)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> auctionService.getAuctionById(id, false))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("Auction not found");

                verify(auctionRepository).findById(id);
        }

        @Test
        void softDeleteAuctionById_Success() {
                when(auctionRepository.findById(id)).thenReturn(Optional.of(auctionEntity));
                when(auctionRepository.save(any())).thenReturn(auctionEntity);

                auctionService.softDeleteAuctionById(id);

                verify(auctionRepository).findById(id);
                verify(auctionRepository).save(argThat(entity -> entity.getIsDeleted()));
        }

        @Test
        void markAuctionsOwnerDeletedByOwnerId_Success() {
                ObjectId ownerId = new ObjectId();

                Auction auction1 = Auction.builder().id(new ObjectId()).ownerId(ownerId).build();
                Auction auction2 = Auction.builder().id(new ObjectId()).ownerId(ownerId).build();

                AuctionEntity entity1 = AuctionMapper.toEntity(auction1);
                AuctionEntity entity2 = AuctionMapper.toEntity(auction2);

                when(auctionRepository.findByOwnerId(ownerId)).thenReturn(List.of(entity1, entity2));
                when(auctionRepository.saveAll(anyList())).thenReturn(List.of(entity1, entity2));

                auctionService.markOwnerDeletedByOwnerId(ownerId);

                verify(auctionRepository).findByOwnerId(ownerId);
                verify(auctionRepository).saveAll(anyList());
        }

        @Test
        void updateAuction_Success() {
                Auction updatedAuction = Auction.builder().title("New Title").build();
                AuctionEntity updatedEntity = AuctionMapper.toEntity(updatedAuction);

                when(auctionRepository.findById(id)).thenReturn(Optional.of(auctionEntity));
                when(auctionRepository.save(any())).thenReturn(updatedEntity);
                when(valueOperations.get(any())).thenReturn(null);

                Auction result = auctionService.updateAuctionById(id, Auction.builder().title("New Title").build());

                assertThat(result.getTitle()).isEqualTo("New Title");
                assertThat(result.isDeleted()).isFalse();
                assertThat(result.isOwnerDeleted()).isFalse();
        }

        @Test
        void getAuctionById_CacheHit() {
                when(valueOperations.get(RedisRepository.AUCTIONS_PREFIX_DELIM + id.toString())).thenReturn(auction);

                Auction result = auctionService.getAuctionById(id, false);

                assertThat(result).isSameAs(auction);
                verify(auctionRepository, never()).findById(any());
        }

        @Test
        void getAuctionById_CacheMiss() {
                when(valueOperations.get(anyString())).thenReturn(null);
                when(auctionRepository.findById(id)).thenReturn(Optional.of(auctionEntity));

                Auction result = auctionService.getAuctionById(id, false);

                assertThat(result).isEqualTo(AuctionMapper.toDomain(auctionEntity));
                verify(auctionRepository).findById(id);
                verify(valueOperations).set(RedisRepository.AUCTIONS_PREFIX_DELIM + id.toString(),
                                AuctionMapper.toDomain(auctionEntity));
        }

        @Test
        void updateAuctionById_Success_UpdatesCache() {
                Auction updates = Auction.builder().title("Updated Title").build();
                Auction updatedAuction = Auction.builder().title("Updated Title").build();
                AuctionEntity updatedEntity = AuctionMapper.toEntity(updatedAuction);

                when(valueOperations.get(anyString())).thenReturn(null);
                when(auctionRepository.findById(id)).thenReturn(Optional.of(auctionEntity));
                when(auctionRepository.save(any())).thenReturn(updatedEntity);

                Auction result = auctionService.updateAuctionById(id, updates);

                assertThat(result.getTitle()).isEqualTo("Updated Title");
                verify(auctionRepository).save(any(AuctionEntity.class));
        }

        @Test
        void updateAuctionById_NotFound() {
                when(valueOperations.get(anyString())).thenReturn(null);
                when(auctionRepository.findById(id)).thenReturn(Optional.empty());

                Auction updates = Auction.builder().title("Doesn't matter").build();

                assertThatThrownBy(() -> auctionService.updateAuctionById(id, updates))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("Auction not found");

                verify(auctionRepository, never()).save(any());
        }

        @Test
        void softDeleteAuctionById_Success_EvictsCache() {
                when(valueOperations.get(anyString())).thenReturn(null);
                when(auctionRepository.findById(id)).thenReturn(Optional.of(auctionEntity));
                when(auctionRepository.save(any())).thenReturn(auctionEntity);

                auctionService.softDeleteAuctionById(id);

                verify(auctionRepository).save(argThat(e -> e.getIsDeleted()));
                verify(redisTemplate).delete(RedisRepository.AUCTIONS_PREFIX_DELIM + id.toString());
        }
}
