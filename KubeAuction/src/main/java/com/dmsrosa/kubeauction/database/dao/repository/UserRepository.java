package com.dmsrosa.kubeauction.database.dao.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.dmsrosa.kubeauction.database.dao.entity.UserEntity;

public interface UserRepository extends MongoRepository<UserEntity, ObjectId> {

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

}
