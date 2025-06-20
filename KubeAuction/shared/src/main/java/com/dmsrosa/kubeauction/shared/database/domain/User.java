package com.dmsrosa.kubeauction.shared.database.domain;

import java.util.UUID;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
