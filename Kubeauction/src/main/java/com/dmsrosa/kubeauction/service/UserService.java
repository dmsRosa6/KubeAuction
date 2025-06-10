package com.dmsrosa.kubeauction.service;

import java.util.Optional;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    @CachePut(value = "userCache", key = "#result.id")
    public UserEntity createUser(UserEntity user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("User already exists. email=%", user.getEmail());
        }

        UserEntity savedUser = userRepository.save(user);

        redisTemplate.opsForValue().set(
                makeRedisKey(user.getEmail(), true),
                savedUser);

        return savedUser;
    }

    @Cacheable(value = "userCache", key = "#id")
    public UserEntity getUserById(ObjectId id, boolean getDeleted) {
        Optional<UserEntity> response = userRepository.findById(id);
        if (response.isEmpty())
            throw new NotFoundException("User not found. id=%s", id.toString());

        UserEntity u = response.get();
        if (!getDeleted && u.getIsDeleted())
            return null;
        return u;
    }

    @Cacheable(value = "userCache", key = "'email::' + #email")
    public UserEntity getUserByEmail(String email, boolean getDeleted) {
        Optional<UserEntity> response = userRepository.findByEmail(email);
        if (response.isEmpty())
            throw new NotFoundException("User not found. email=%s", email);

        UserEntity u = response.get();
        if (!getDeleted && u.getIsDeleted())
            return null;

        redisTemplate.opsForValue().set(
                makeRedisKey(u.getId().toString(), false),
                u);

        redisTemplate.opsForValue().set(
                makeRedisKey(u.getEmail(), true),
                u);

        return u;
    }

    @CacheEvict(value = "userCache", key = "#id")
    public void softDeleteUserById(ObjectId id) {
        UserEntity response = this.getUserById(id, false);
        response.setIsDeleted(true);
        this.userRepository.save(response);
    }

    public void softDeleteUserByEmail(String email) {
        UserEntity response = this.getUserByEmail(email, false);
        response.setIsDeleted(true);
        this.userRepository.save(response);
        redisTemplate.delete(makeRedisKey(email, true));
    }

    @CachePut(value = "userCache", key = "#result.id")
    public UserEntity updateUserByEmail(String email, UserEntity update) {
        UserEntity user = this.getUserByEmail(email, false);

        updateUser(user, update);
        UserEntity savedUser = this.userRepository.save(user);

        if (email.equals(savedUser.getEmail())) {
            redisTemplate.opsForValue().set(
                    makeRedisKey(email, true),
                    savedUser);
        }

        return savedUser;
    }

    @CachePut(value = "userCache", key = "#result.id")
    public UserEntity updateUserById(ObjectId id, UserEntity update) {
        UserEntity user = this.getUserById(id, false);

        if (user == null) {
            throw new NotFoundException("User was not found. id=%s", id.toString());
        }

        updateUser(user, update);
        UserEntity savedUser = this.userRepository.save(user);

        redisTemplate.opsForValue().set(
                "userCache::email::" + savedUser.getEmail(),
                savedUser);

        return savedUser;
    }

    // private methods
    private void updateUser(UserEntity user, UserEntity update) {
        String name = update.getName();
        String nickname = update.getNickname();
        String pwd = update.getPwd();
        UUID photoId = update.getPhotoId();

        if (name != null && !name.isBlank())
            user.setName(name);
        if (nickname != null && !nickname.isBlank())
            user.setNickname(nickname);
        if (pwd != null && !pwd.isBlank()) {
            String digest = passwordEncoder.encode(pwd);
            user.setPwd(digest);
        }
        if (photoId != null)
            user.setPhotoId(photoId);
    }

    private String makeRedisKey(String id, boolean isEmailId) {
        if (isEmailId)
            return RedisConfig.USERS_PREFIX_DELIM + "::email" + id;
        return RedisConfig.USERS_PREFIX_DELIM + id;
    }
}