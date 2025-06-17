package com.dmsrosa.kubeauction.service;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.config.RedisConfig;
import com.dmsrosa.kubeauction.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.database.dao.repository.UserRepository;
import com.dmsrosa.kubeauction.service.exception.ConflictException;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    private String keyById(ObjectId id) {
        return RedisConfig.USERS_PREFIX_DELIM + id.toString();
    }

    private String keyByEmail(String email) {
        return RedisConfig.USERS_PREFIX_DELIM + "email::" + email;
    }

    public UserEntity createUser(UserEntity user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("User already exists. email=%s", user.getEmail());
        }
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        user.setIsDeleted(false);

        UserEntity saved = userRepository.save(user);
        redisTemplate.opsForValue().set(keyById(saved.getId()), saved);
        redisTemplate.opsForValue().set(keyByEmail(saved.getEmail()), saved);
        return saved;
    }

    public UserEntity getUserById(ObjectId id, boolean includeDeleted) {
        String key = keyById(id);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof UserEntity) {
            UserEntity u = (UserEntity) cached;
            if (includeDeleted || !u.getIsDeleted()) {
                return u;
            }
        }
        Optional<UserEntity> opt = userRepository.findById(id);
        UserEntity u = opt.filter(x -> includeDeleted || !x.getIsDeleted())
                .orElseThrow(() -> new NotFoundException("User not found. id=%s", id.toString()));
        redisTemplate.opsForValue().set(key, u);
        redisTemplate.opsForValue().set(keyByEmail(u.getEmail()), u);
        return u;
    }

    public UserEntity getUserByEmail(String email, boolean includeDeleted) {
        String keyE = keyByEmail(email);
        Object cached = redisTemplate.opsForValue().get(keyE);
        if (cached instanceof UserEntity u) {
            if (includeDeleted || !u.getIsDeleted()) {
                return u;
            }
        }
        Optional<UserEntity> opt = userRepository.findByEmail(email);
        UserEntity u = opt.filter(x -> includeDeleted || !x.getIsDeleted())
                .orElseThrow(() -> new NotFoundException("User not found. email=%s", email));

        redisTemplate.opsForValue().set(keyById(u.getId()), u);
        redisTemplate.opsForValue().set(keyE, u);
        return u;
    }

    public void softDeleteUserById(ObjectId id) {
        UserEntity u = getUserById(id, false);
        u.setIsDeleted(true);
        userRepository.save(u);
        redisTemplate.delete(keyById(id));
        redisTemplate.delete(keyByEmail(u.getEmail()));
    }

    public void softDeleteUserByEmail(String email) {
        UserEntity u = getUserByEmail(email, false);
        u.setIsDeleted(true);
        userRepository.save(u);
        redisTemplate.delete(keyByEmail(email));
        redisTemplate.delete(keyById(u.getId()));
    }

    public UserEntity updateUserById(ObjectId id, UserEntity update) {
        UserEntity u = getUserById(id, false);
        applyUpdates(u, update);
        UserEntity saved = userRepository.save(u);
        redisTemplate.opsForValue().set(keyById(id), saved);
        redisTemplate.opsForValue().set(keyByEmail(saved.getEmail()), saved);
        return saved;
    }

    public UserEntity updateUserByEmail(String email, UserEntity update) {
        UserEntity u = getUserByEmail(email, false);
        applyUpdates(u, update);
        UserEntity saved = userRepository.save(u);
        redisTemplate.opsForValue().set(keyById(saved.getId()), saved);
        redisTemplate.opsForValue().set(keyByEmail(email), saved);
        return saved;
    }

    // private methods
    private void applyUpdates(UserEntity u, UserEntity update) {
        if (update.getName() != null && !update.getName().isBlank()) {
            u.setName(update.getName());
        }
        if (update.getNickname() != null && !update.getNickname().isBlank()) {
            u.setNickname(update.getNickname());
        }
        if (update.getPwd() != null && !update.getPwd().isBlank()) {
            u.setPwd(passwordEncoder.encode(update.getPwd()));
        }
        if (update.getPhotoId() != null) {
            u.setPhotoId(update.getPhotoId());
        }
    }
}
