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
public class UpdateUserDto {
    private String name;

    private String nickname;

    private UUID photoId;

    private String pwd;

    public static User ToUserEntity(UpdateUserDto user) {
        return User.builder().name(user.getName()).nickname(user.getNickname())
                .pwd(user.getPwd()).photoId(user.getPhotoId()).build();
    }
}
