package com.dmsrosa.kubeauction.dto.user;

import java.util.UUID;

import com.dmsrosa.kubeauction.shared.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.shared.database.domain.User;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonDeserialize(builder = UserResponseDto.UserResponseDtoBuilder.class)
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private String id;

    private String name;

    private String nickname;

    private String email;

    private UUID photoId;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserResponseDtoBuilder {
    }

    public static UserResponseDto toUserResponseDto(User user) {
        return UserResponseDto.builder().id(user.getId().toString()).name(user.getName()).nickname(user.getNickname())
                .email(user.getEmail()).photoId(user.getPhotoId()).build();
    }
}