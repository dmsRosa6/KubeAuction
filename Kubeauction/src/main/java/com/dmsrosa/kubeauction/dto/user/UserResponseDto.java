package com.dmsrosa.kubeauction.dto.user;

import java.util.UUID;

import com.dmsrosa.kubeauction.database.dao.entity.UserEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    private String id;

    private String name;

    private String nickname;

    private String email;

    private UUID photoId;

    public static UserResponseDto toUserResponseDto(UserEntity user) {
        return UserResponseDto.builder().id(user.getId().toString()).name(user.getName()).nickname(user.getNickname())
                .email(user.getEmail()).photoId(user.getPhotoId()).build();
    }
}