package com.dmsrosa.kubeauction.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.exception.ConflictException;
import com.dmsrosa.kubeauction.exception.NotFoundException;
import com.dmsrosa.kubeauction.shared.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.shared.database.dao.repository.UserRepository;
import com.dmsrosa.kubeauction.shared.database.domain.User;
import com.dmsrosa.kubeauction.shared.mapper.UserMapper;
import com.dmsrosa.kubeauction.shared.redis.RedisRepository;
import com.dmsrosa.kubeauction.shared.utils.Pair;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisRepository redis;

    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RedisRepository redisRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redis = redisRepository;
    }

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("User already exists. email=%s", user.getEmail());
        }

        user.setPwd(passwordEncoder.encode(user.getPwd()));
        user.setDeleted(false);

        UserEntity savedEntity = userRepository.save(UserMapper.toEntity(user));
        User savedUser = UserMapper.toDomain(savedEntity);

        Map<String, Pair<Object, Boolean>> m = new HashMap<>(2);

        m.put(savedUser.getEmail(), new Pair<>(savedUser, true));
        m.put(savedUser.getId().toString(), new Pair<>(savedUser, false));

        redis.redisMultiSetWithVariants(m);

        return savedUser;
    }

    public User getUserById(ObjectId id, boolean includeDeleted) {

        User cached = redis.redisGet(id.toString(), User.class);

        if (includeDeleted || !cached.isDeleted()) {
            return cached;
        }

        Optional<UserEntity> opt = userRepository.findById(id);

        if (opt.isEmpty())
            throw new NotFoundException("User not found. id=%s", id.toString());

        User user = UserMapper.toDomain(opt.get());

        if (includeDeleted || !user.isDeleted())
            throw new NotFoundException("User not found. id=%s", id.toString());

        Map<String, Pair<Object, Boolean>> m = new HashMap<>(2);

        m.put(user.getEmail(), new Pair<>(user, true));
        m.put(user.getId().toString(), new Pair<>(user, false));

        redis.redisMultiSetWithVariants(m);

        return user;
    }

    public User getUserByEmail(String email, boolean includeDeleted) {

        User cached = redis.redisGet(email, User.class, true);

        if (includeDeleted || !cached.isDeleted()) {
            return cached;
        }

        Optional<UserEntity> opt = userRepository.findByEmail(email);

        if (opt.isEmpty())
            throw new NotFoundException("User not found. email=%s", email);

        User user = UserMapper.toDomain(opt.get());

        if (includeDeleted || !user.isDeleted())
            throw new NotFoundException("User not found. email=%s", email);

        Map<String, Pair<Object, Boolean>> m = new HashMap<>(2);

        m.put(user.getEmail(), new Pair<>(user, true));
        m.put(user.getId().toString(), new Pair<>(user, false));

        redis.redisMultiSetWithVariants(m);

        return user;
    }

    public void softDeleteUserById(ObjectId id) {
        User user = getUserById(id, false);
        UserEntity userEntity = UserMapper.toEntity(user);

        userEntity.setIsDeleted(true);
        userRepository.save(userEntity);

        redis.redisDelete(id.toString(), User.class);
    }

    public void softDeleteUserByEmail(String email) {
        User user = getUserByEmail(email, false);
        UserEntity userEntity = UserMapper.toEntity(user);

        userEntity.setIsDeleted(true);
        userRepository.save(userEntity);

        redis.redisDelete(email, User.class, true);
    }

    public User updateUserById(ObjectId id, User update) {
        User user = getUserById(id, false);
        applyUpdates(user, update);

        UserEntity userEntity = UserMapper.toEntity(user);

        userRepository.save(userEntity);

        Map<String, Pair<Object, Boolean>> m = new HashMap<>(2);

        m.put(user.getEmail(), new Pair<>(user, true));
        m.put(user.getId().toString(), new Pair<>(user, false));

        redis.redisMultiSetWithVariants(m);

        return user;
    }

    public User updateUserByEmail(String email, User update) {
        User user = getUserByEmail(email, false);
        applyUpdates(user, update);

        UserEntity userEntity = UserMapper.toEntity(user);

        userRepository.save(userEntity);

        Map<String, Pair<Object, Boolean>> m = new HashMap<>(2);

        m.put(user.getEmail(), new Pair<>(user, true));
        m.put(user.getId().toString(), new Pair<>(user, false));

        redis.redisMultiSetWithVariants(m);

        return user;
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

    private void applyUpdates(User u, User update) {
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
