package com.dmsrosa.kubeauction.database.dao.entity;

import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "users")
public class UserEntity {

    @Id
    private ObjectId id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String nickname;

    private String pwd; // hashed

    private UUID photoId;

    @Builder.Default
    private Boolean isDeleted = false;

}
