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

import com.dmsrosa.kubeauction.config.RedisConfig;
import com.dmsrosa.kubeauction.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.database.dao.repository.AuctionRepository;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

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
        private AuctionEntity auction;

        @BeforeEach
        void setup() {
                MockitoAnnotations.openMocks(this);
                id = new ObjectId();
                auction = AuctionEntity.builder()
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

                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void createAuction_Success() {
                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
                AuctionEntity auction = AuctionEntity.builder()
                                .id(new ObjectId())
                                .title("Test Auction")
                                .descr("Description")
                                .imageId(UUID.randomUUID())
                                .ownerId(new ObjectId())
                                .endDate(new Date(System.currentTimeMillis() + 86400000))
                                .minimumPrice(100)
                                .isDeleted(false)
                                .ownerDeleted(false)
                                .build();

                when(auctionRepository.save(any(AuctionEntity.class))).thenReturn(auction);
                when(userService.getUserById(any(), anyBoolean())).thenReturn(null);
                AuctionEntity result = auctionService.createAuction(auction);

                assertThat(result).isNotNull();
                assertThat(result.getTitle()).isEqualTo("Test Auction");
                assertThat(result.getIsDeleted()).isFalse();
                assertThat(result.getOwnerDeleted()).isFalse();

                verify(auctionRepository, times(1)).save(any(AuctionEntity.class));
        }

        @Test
        void getAuctionById_NotFound() {
                ObjectId id = new ObjectId();
                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
                when(auctionRepository.findById(id)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> auctionService.getAuctionById(id, false))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("Auction not found");

                verify(auctionRepository, times(1)).findById(id);
        }

        @Test
        void softDeleteAuctionById_Success() {
                ObjectId id = new ObjectId();
                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
                AuctionEntity auction = AuctionEntity.builder()
                                .id(id)
                                .title("Auction to delete")
                                .isDeleted(false)
                                .ownerDeleted(false)
                                .build();

                when(auctionRepository.findById(id)).thenReturn(Optional.of(auction));
                when(auctionRepository.save(any(AuctionEntity.class))).thenAnswer(i -> i.getArgument(0));

                auctionService.softDeleteAuctionById(id);

                verify(auctionRepository, times(1)).findById(id);
                verify(auctionRepository, times(1)).save(argThat(a -> a.getIsDeleted() == true));
        }

        @Test
        void markAuctionsOwnerDeletedByOwnerId_Success() {
                ObjectId ownerId = new ObjectId();

                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
                AuctionEntity auction1 = AuctionEntity.builder()
                                .id(new ObjectId())
                                .ownerId(ownerId)
                                .isDeleted(false)
                                .ownerDeleted(false)
                                .build();

                AuctionEntity auction2 = AuctionEntity.builder()
                                .id(new ObjectId())
                                .ownerId(ownerId)
                                .isDeleted(false)
                                .ownerDeleted(false)
                                .build();

                AuctionEntity auction1After = AuctionEntity.builder()
                                .id(new ObjectId())
                                .ownerId(ownerId)
                                .isDeleted(false)
                                .ownerDeleted(true)
                                .build();

                AuctionEntity auction2After = AuctionEntity.builder()
                                .id(new ObjectId())
                                .ownerId(ownerId)
                                .isDeleted(false)
                                .ownerDeleted(true)
                                .build();

                when(auctionRepository.findByOwnerId(ownerId)).thenReturn(List.of(auction1, auction2));
                when(auctionRepository.saveAll(anyList())).thenReturn(List.of(auction1After, auction2After));

                auctionService.markOwnerDeletedByOwnerId(ownerId);

                verify(auctionRepository, times(1)).findByOwnerId(ownerId);
                verify(auctionRepository, times(1)).saveAll(anyList());
        }

        @Test
        void updateAuction_Sucess() {
                ObjectId ownerId = new ObjectId();

                AuctionEntity auction1 = AuctionEntity.builder()
                                .id(new ObjectId())
                                .ownerId(ownerId)
                                .title("Test Auction")
                                .isDeleted(false)
                                .ownerDeleted(false)
                                .build();

                AuctionEntity updates = AuctionEntity.builder()
                                .title("New Title")
                                .build();

                when(auctionRepository.findById(ownerId)).thenReturn(Optional.of(auction1));
                when(auctionRepository.save(any())).thenReturn(auction1);
                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
                when(valueOperations.get(any())).thenReturn("");

                var updated = auctionService.updateAuctionById(ownerId, updates);

                assertThat(updated).isNotNull();
                assertThat(updated.getTitle()).isEqualTo("New Title");
                assertThat(updated.getIsDeleted()).isFalse();
                assertThat(updated.getOwnerDeleted()).isFalse();
        }

        @Test
        void getAuctionById_CacheHit() {
                when(valueOperations.get(RedisConfig.AUCTIONS_PREFIX_DELIM + id.toString())).thenReturn(auction);

                AuctionEntity result = auctionService.getAuctionById(id, false);

                assertThat(result).isSameAs(auction);
                verify(auctionRepository, never()).findById(any());
        }

        @Test
        void getAuctionById_CacheMiss() {
                when(valueOperations.get(anyString())).thenReturn(null);
                when(auctionRepository.findById(id)).thenReturn(Optional.of(auction));

                AuctionEntity result = auctionService.getAuctionById(id, false);

                assertThat(result).isSameAs(auction);
                verify(auctionRepository).findById(id);
                verify(valueOperations).set(RedisConfig.AUCTIONS_PREFIX_DELIM + id.toString(), auction);
        }

        @Test
        void updateAuctionById_Success_UpdatesCache() {
                when(valueOperations.get(anyString())).thenReturn(null);
                when(auctionRepository.findById(id)).thenReturn(Optional.of(auction));
                when(auctionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

                AuctionEntity updates = AuctionEntity.builder()
                                .title("New Title")
                                .build();

                AuctionEntity updated = auctionService.updateAuctionById(id, updates);

                assertThat(updated.getTitle()).isEqualTo("New Title");
                verify(auctionRepository).save(auction);
        }

        @Test
        void updateAuctionById_NotFound() {
                when(valueOperations.get(anyString())).thenReturn(null);
                when(auctionRepository.findById(id)).thenReturn(Optional.empty());

                AuctionEntity updates = AuctionEntity.builder()
                                .title("Whatever")
                                .build();

                assertThatThrownBy(() -> auctionService.updateAuctionById(id, updates))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("Auction not found");
                verify(auctionRepository).findById(id);
                verify(auctionRepository, never()).save(any());
        }

        @Test
        void softDeleteAuctionById_Success_EvictsCache() {
                when(valueOperations.get(anyString())).thenReturn(null);
                when(auctionRepository.findById(id)).thenReturn(Optional.of(auction));

                auctionService.softDeleteAuctionById(id);

                verify(auctionRepository).save(argThat(a -> a.getIsDeleted()));
                verify(redisTemplate).delete("auctionCache::" + id.toString());
        }
}
