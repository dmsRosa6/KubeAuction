package com.dmsrosa.kubeauction.database.domain;

import java.util.UUID;

import org.bson.types.ObjectId;

import lombok.Data;

@Data
public class User {
    private ObjectId id;

    private String name;

    private String email;

    private String nickname;

    private String pwd;

    private UUID photoId;
}
