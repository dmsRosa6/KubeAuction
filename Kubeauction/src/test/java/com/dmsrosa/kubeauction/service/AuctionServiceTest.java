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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.dmsrosa.kubeauction.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.database.dao.repository.AuctionRepository;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

public class AuctionServiceTest {

        @Mock
        private AuctionRepository auctionRepository;

        @InjectMocks
        private AuctionService auctionService;

        @BeforeEach
        void setup() {
                MockitoAnnotations.openMocks(this);
        }

        @Test
        void createAuction_Success() {
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
                when(auctionRepository.findById(id)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> auctionService.getAuctionById(id, false))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("Auction not found");

                verify(auctionRepository, times(1)).findById(id);
        }

        @Test
        void softDeleteAuctionById_Success() {
                ObjectId id = new ObjectId();
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

}
