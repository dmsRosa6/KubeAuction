package com.dmsrosa.kubeauction.dto.user;

import java.util.UUID;

import com.dmsrosa.kubeauction.shared.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.shared.database.domain.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {

    private String name;

    private String email;

    private String nickname;

    private String pwd;

    private UUID photoId;

    public static User ToUserEntity(CreateUserDto user) {
        return User.builder().email(user.getEmail()).name(user.getName()).nickname(user.getNickname())
                .pwd(user.getPwd()).photoId(user.getPhotoId()).build();
    }
}
