package com.dmsrosa.kubeauction.shared.database.domain;

import java.util.UUID;

import org.bson.types.ObjectId;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private ObjectId id;

    private String name;

    private String email;

    private String nickname;

    private String pwd;

    private UUID photoId;

    private boolean isDeleted;
}
