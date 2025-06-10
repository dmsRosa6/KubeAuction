package com.dmsrosa.kubeauction.dto.user;

import java.util.UUID;

import com.dmsrosa.kubeauction.database.dao.entity.UserEntity;

import lombok.Data;

@Data
public class UpdateUserDto {
    private String name;

    private String nickname;

    private UUID photoId;

    private String pwd;

    public static UserEntity ToUserEntity(UpdateUserDto user) {
        return UserEntity.builder().name(user.getName()).nickname(user.getNickname())
                .pwd(user.getPwd()).photoId(user.getPhotoId()).build();
    }
}
