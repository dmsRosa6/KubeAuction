package com.dmsrosa.kubeauction.dto.user;

import java.util.UUID;

import com.dmsrosa.kubeauction.database.dao.entity.UserEntity;

import lombok.Data;

@Data
public class CreateUserDto {

    private String name;

    private String email;

    private String nickname;

    private String pwd;

    private UUID photoId;

    public static UserEntity ToUserEntity(CreateUserDto user) {
        return UserEntity.builder().email(user.getEmail()).name(user.getName()).nickname(user.getNickname())
                .pwd(user.getNickname()).photoId(user.getPhotoId()).build();
    }
}
