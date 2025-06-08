package com.dmsrosa.kubeauction.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeauction.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.database.dao.repository.UserRepository;
import com.dmsrosa.kubeauction.service.exception.ConflictException;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity register(String name, String email, String nickname, String pwd) {
        UserEntity u = UserEntity.builder()
                .name(name)
                .nickname(nickname)
                .pwd(pwd)
                .email(email)
                .build();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User already exists. email=%", email);
        }

        return userRepository.save(u);
    }

    public UserEntity getUserById(ObjectId id, boolean getDeleted) {
        Optional<UserEntity> response = userRepository.findById(id);

        if (response.isEmpty())
            throw new NotFoundException("User not found. id=%s", id.toString());

        UserEntity u = response.get();
        if (!getDeleted && u.getIsDeleted())
            return null;

        return u;
    }

    public UserEntity getUserByEmail(String email, boolean getDeleted) {
        Optional<UserEntity> response = userRepository.findByEmail(email);

        if (response.isEmpty())
            throw new NotFoundException("User not found. email=%s", email);

        UserEntity u = response.get();
        if (!getDeleted && u.getIsDeleted())
            return null;

        return u;
    }

    public void deleteById(ObjectId id) {
        UserEntity response = this.getUserById(id, false);

        response.setIsDeleted(true);
        this.userRepository.save(response);
    }

    public void deleteByEmail(String email) {
        UserEntity response = this.getUserByEmail(email, false);

        response.setIsDeleted(true);
        this.userRepository.save(response);
    }

    public UserEntity UpdateUserByEmail(String email, UserEntity update) {
        UserEntity user = this.getUserByEmail(email, false);

        if (user == null) {
            throw new NotFoundException("User was not found. email=%s", email);
        }

        updateUser(user, update);

        return this.userRepository.save(user);
    }

    public UserEntity UpdateUserById(ObjectId id, UserEntity update) {
        UserEntity user = this.getUserById(id, false);

        if (user == null) {
            throw new NotFoundException("User was not found. id=%s", id.toString());
        }

        updateUser(user, update);

        return this.userRepository.save(user);
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
}
