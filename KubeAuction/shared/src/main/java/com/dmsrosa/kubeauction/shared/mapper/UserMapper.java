package com.dmsrosa.kubeauction.shared.mapper;

import com.dmsrosa.kubeauction.shared.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.shared.database.domain.User;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setName(entity.getName());
        user.setEmail(entity.getEmail());
        user.setNickname(entity.getNickname());
        user.setPwd(entity.getPwd());
        user.setPhotoId(entity.getPhotoId());
        user.setDeleted(entity.getIsDeleted());
        return user;
    }

    public static UserEntity toEntity(User domain) {
        return UserEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .email(domain.getEmail())
                .nickname(domain.getNickname())
                .pwd(domain.getPwd())
                .photoId(domain.getPhotoId())
                .isDeleted(domain.isDeleted())
                .build();
    }
}
