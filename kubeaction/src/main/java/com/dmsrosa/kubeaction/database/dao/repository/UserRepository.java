package com.dmsrosa.kubeaction.database.dao.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.dmsrosa.kubeaction.database.dao.entity.UserEntity;

public interface UserRepository extends MongoRepository<UserEntity, ObjectId> {

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

}
