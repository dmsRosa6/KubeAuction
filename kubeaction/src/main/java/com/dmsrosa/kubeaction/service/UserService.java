package com.dmsrosa.kubeaction.service;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.dmsrosa.kubeaction.database.dao.entity.UserEntity;
import com.dmsrosa.kubeaction.database.dao.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        if (!response.isPresent())
            return null;

        UserEntity u = response.get();
        if (!getDeleted && u.getIsDeleted())
            return null;

        return u;
    }

    public UserEntity getUserByEmail(String email, boolean getDeleted) {
        Optional<UserEntity> response = userRepository.findByEmail(email);

        if (!response.isPresent())
            return null;

        UserEntity u = response.get();
        if (!getDeleted && u.getIsDeleted())
            return null;

        return u;
    }

    public void deleteById(ObjectId id) {
        UserEntity response = this.getUserById(id, false);

        if (response == null) {
            throw new NotFoundException("User was not found. id=%s", id.toHexString());
        }

        response.setIsDeleted(true);
        this.userRepository.save(response);
    }

    public void deleteByEmail(String email) {
        UserEntity response = this.getUserByEmail(email, false);

        if (response == null) {
            throw new NotFoundException("User was not found. email=%s", email);
        }

        response.setIsDeleted(true);
        this.userRepository.save(response);
    }

    public UserEntity UpdateUserByEmail(String email, UserEntity update) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'UpdateUserByEmail'");
    }

    public UserEntity UpdateUserById(ObjectId id, UserEntity update) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'UpdateUserById'");
    }
}
