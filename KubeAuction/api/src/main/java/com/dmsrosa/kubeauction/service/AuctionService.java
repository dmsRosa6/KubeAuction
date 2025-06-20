package com.dmsrosa.kubeauction.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.exception.NotFoundException;
import com.dmsrosa.kubeauction.shared.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.shared.database.dao.repository.AuctionRepository;
import com.dmsrosa.kubeauction.shared.database.domain.Auction;
import com.dmsrosa.kubeauction.shared.mapper.AuctionMapper;
import com.dmsrosa.kubeauction.shared.redis.RedisRepository;
import com.mongodb.internal.connection.Time;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final UserService userService;
    private final RedisRepository redis;

    public AuctionService(AuctionRepository auctionRepository, RedisRepository redis,
            UserService userService) {
        this.auctionRepository = auctionRepository;
        this.redis = redis;
        this.userService = userService;
    }

    public Auction createAuction(Auction auction) {
        userService.getUserById(auction.getOwnerId(), false);

        AuctionEntity saved = auctionRepository.save(AuctionMapper.toEntity(auction));
        Auction a = AuctionMapper.toDomain(saved);
        redis.redisSet(a.getId().toString(), saved);
        redis.redisZAdd(RedisRepository.AUCTIONS_NOTIFICATIONS_KEY, a.getId().toString(), a.getEndDate().getTime());
        return a;
    }

    public Auction getAuctionById(ObjectId id, boolean includeDeleted) {

        Auction cached = redis.redisGet(id.toString(), Auction.class);

        if (includeDeleted || !cached.isDeleted())
            return cached;

        Optional<AuctionEntity> o = auctionRepository.findById(id);

        if (o.isEmpty())
            throw new NotFoundException("Auction not found.id=%s", id.toString());

        Auction auction = AuctionMapper.toDomain(o.get());

        if (includeDeleted || !auction.isDeleted())
            throw new NotFoundException("Auction not found.id=%s", id.toString());

        redis.redisSet(id.toString(), auction);
        return auction;
    }

    public Auction updateAuctionById(ObjectId id, Auction updates) {
        Auction existing = getAuctionById(id, false);

        if (updates.getTitle() != null)
            existing.setTitle(updates.getTitle());
        if (updates.getDescr() != null)
            existing.setDescr(updates.getDescr());
        if (updates.getImageId() != null)
            existing.setImageId(updates.getImageId());

        AuctionEntity saved = auctionRepository.save(AuctionMapper.toEntity(existing));

        Auction auction = AuctionMapper.toDomain(saved);

        redis.redisSet(auction.getId().toString(), auction);
        return auction;
    }

    public void softDeleteAuctionById(ObjectId id) {
        Auction auction = getAuctionById(id, false);
        AuctionEntity entity = AuctionMapper.toEntity(auction);

        entity.setIsDeleted(true);
        auctionRepository.save(entity);

        redis.redisDelete(id.toString(), Auction.class);
    }

    public void markOwnerDeletedByOwnerId(ObjectId ownerId) {
        List<AuctionEntity> list = auctionRepository.findByOwnerId(ownerId);

        Map<String, Object> updates = new HashMap<>();
        list.forEach(a -> {
            a.setOwnerDeleted(true);
            updates.put(a.getId().toString(), a);
        });

        redis.redisMultiSet(updates);
        auctionRepository.saveAll(list);
    }
}
