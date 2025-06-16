package com.dmsrosa.kubeauction.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.config.RedisConfig;
import com.dmsrosa.kubeauction.database.dao.entity.AuctionEntity;
import com.dmsrosa.kubeauction.database.dao.repository.AuctionRepository;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;

    private String key(ObjectId id) {
        return RedisConfig.AUCTIONS_PREFIX_DELIM + id.toString();
    }

    public AuctionEntity createAuction(AuctionEntity auction) {
        userService.getUserById(auction.getOwnerId(), false);

        AuctionEntity saved = auctionRepository.save(auction);
        redisTemplate.opsForValue().set(key(saved.getId()), saved);
        return saved;
    }

    public AuctionEntity getAuctionById(ObjectId id, boolean includeDeleted) {
        String cacheKey = key(id);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof AuctionEntity) {
            AuctionEntity ae = (AuctionEntity) cached;
            if (includeDeleted || !ae.getIsDeleted()) {
                return ae;
            }
        }

        Optional<AuctionEntity> o = auctionRepository.findById(id);
        AuctionEntity entity = o.filter(a -> includeDeleted || !a.getIsDeleted())
                .orElseThrow(() -> new NotFoundException("Auction not found.id=%s", id.toString()));

        redisTemplate.opsForValue().set(cacheKey, entity);
        return entity;
    }

    public AuctionEntity updateAuctionById(ObjectId id, AuctionEntity updates) {
        AuctionEntity existing = getAuctionById(id, false);

        if (updates.getTitle() != null)
            existing.setTitle(updates.getTitle());
        if (updates.getDescr() != null)
            existing.setDescr(updates.getDescr());
        if (updates.getImageId() != null)
            existing.setImageId(updates.getImageId());

        AuctionEntity saved = auctionRepository.save(existing);

        redisTemplate.opsForValue().set(key(id), saved);
        return saved;
    }

    public void softDeleteAuctionById(ObjectId id) {
        AuctionEntity entity = getAuctionById(id, false);
        entity.setIsDeleted(true);
        auctionRepository.save(entity);

        redisTemplate.delete(key(id));
    }

    public void markOwnerDeletedByOwnerId(ObjectId ownerId) {
        List<AuctionEntity> list = auctionRepository.findByOwnerId(ownerId);

        Map<String, Object> updates = new HashMap<>();
        list.forEach(a -> {
            a.setOwnerDeleted(true);
            updates.put(key(a.getId()), a);
        });

        redisTemplate.opsForValue().multiSet(updates);
        auctionRepository.saveAll(list);
    }
}
