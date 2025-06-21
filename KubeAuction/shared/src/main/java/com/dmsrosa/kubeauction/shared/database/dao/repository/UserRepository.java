package com.dmsrosa.kubeauction.shared.database.dao.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.dmsrosa.kubeauction.shared.database.dao.entity.UserEntity;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, ObjectId> {

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

}
