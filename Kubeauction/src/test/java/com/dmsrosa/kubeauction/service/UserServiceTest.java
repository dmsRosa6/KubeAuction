package com.dmsrosa.kubeauction.service;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.dmsrosa.kubeauction.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.database.dao.repository.UserRepository;
import com.dmsrosa.kubeauction.service.exception.ConflictException;
import com.dmsrosa.kubeauction.service.exception.NotFoundException;

public class UserServiceTest {
        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private UserService userService;

        @BeforeEach
        void setup() {
                MockitoAnnotations.openMocks(this);
        }

        @Test
        void register_savesUserCorrectly() {

                String name = "Bob";
                String nickname = "bobby";
                String pwd = "hashed";
                String email = "email@email.com";
                UserEntity savedUser = UserEntity.builder()
                                .id(new ObjectId())
                                .name(name)
                                .email(email)
                                .nickname(nickname)
                                .pwd(pwd)
                                .photoId(UUID.randomUUID())
                                .build();

                when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

                UserEntity result = userService.createUser(savedUser);

                assertThat(result.getName()).isEqualTo(name);
                assertThat(result.getEmail()).isEqualTo(email);
                assertThat(result.getNickname()).isEqualTo(nickname);
                assertThat(result.getPwd()).isEqualTo(pwd);
                assertThat(result.getId()).isNotNull();
                assertThat(result.getIsDeleted()).isEqualTo(false);

                when(userRepository.existsByEmail(email)).thenReturn(false);

                verify(userRepository, times(1)).save(any(UserEntity.class));
                verify(userRepository, times(1)).existsByEmail(email);
        }

        @Test
        void register_DuplicateEmailError() {
                String name = "Bob";
                String nickname = "bobby";
                String pwd = "hashed";
                String email = "email@email.com";

                UserEntity user = UserEntity.builder()
                                .id(new ObjectId())
                                .name(name)
                                .email(email)
                                .nickname(nickname)
                                .pwd(pwd)
                                .photoId(UUID.randomUUID())
                                .build();

                when(userRepository.existsByEmail(email)).thenReturn(false);

                when(userRepository.save(any(UserEntity.class))).thenReturn(user);

                UserEntity result = userService.createUser(user);
                assertThat(result.getName()).isEqualTo(name);
                assertThat(result.getId()).isNotNull();

                when(userRepository.existsByEmail(email)).thenReturn(true);

                assertThatThrownBy(() -> userService.createUser(user))
                                .isInstanceOf(ConflictException.class)
                                .hasMessageContaining("User already exists");

                verify(userRepository, times(1)).save(any(UserEntity.class));
                verify(userRepository, times(2)).existsByEmail(email);
        }

        @Test
        void getUserById_Success() {
                ObjectId id = new ObjectId();
                String name = "Bob";
                String email = "email@email.com";
                String nickname = "bobby";
                String pwd = "hashed";

                UserEntity user = UserEntity.builder()
                                .id(id)
                                .name(name)
                                .email(email)
                                .nickname(nickname)
                                .pwd(pwd)
                                .photoId(UUID.randomUUID())
                                .build();

                when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

                UserEntity found = userService.getUserById(user.getId(), false);

                assertThat(found.getName()).isEqualTo(name);
                assertThat(found.getNickname()).isEqualTo(nickname);
                assertThat(found.getEmail()).isEqualTo(email);
                assertThat(found.getIsDeleted()).isEqualTo(false);
                verify(userRepository, times(1)).findById(id);
        }

        @Test
        void getUserById_NotFound() {
                ObjectId id = new ObjectId();
                String name = "Bob";
                String email = "email@email.com";
                String nickname = "bobby";
                String pwd = "hashed";

                UserEntity user = UserEntity.builder()
                                .id(id)
                                .name(name)
                                .email(email)
                                .nickname(nickname)
                                .pwd(pwd)
                                .photoId(UUID.randomUUID())
                                .build();

                when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

                UserEntity response = userService.getUserById(user.getId(), false);

                assertThat(response).isEqualTo(null);
                verify(userRepository, times(1)).findById(id);
        }

        @Test
        void getUserByEmail_Success() {
                ObjectId id = new ObjectId();
                String name = "Bob";
                String email = "email@email.com";
                String nickname = "bobby";
                String pwd = "hashed";

                UserEntity user = UserEntity.builder()
                                .id(id)
                                .name(name)
                                .email(email)
                                .nickname(nickname)
                                .pwd(pwd)
                                .photoId(UUID.randomUUID())
                                .build();

                when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

                UserEntity found = userService.getUserByEmail(user.getEmail(), false);

                assertThat(found.getName()).isEqualTo(name);
                assertThat(found.getNickname()).isEqualTo(nickname);
                assertThat(found.getEmail()).isEqualTo(email);
                assertThat(found.getIsDeleted()).isEqualTo(false);

                verify(userRepository, times(1)).findByEmail(email);
        }

        @Test
        void getUserByEmail_NotFound() {
                ObjectId id = new ObjectId();
                String name = "Bob";
                String email = "email@email.com";
                String nickname = "bobby";
                String pwd = "hashed";

                UserEntity user = UserEntity.builder()
                                .id(id)
                                .name(name)
                                .email(email)
                                .nickname(nickname)
                                .pwd(pwd)
                                .photoId(UUID.randomUUID())
                                .build();

                when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

                UserEntity response = userService.getUserByEmail(user.getEmail(), false);

                assertThat(response).isEqualTo(null);
                verify(userRepository, times(1)).findByEmail(email);
        }

        @Test
        void deleteUserById_Success() {
                ObjectId id = new ObjectId();
                UserEntity userBefore = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email("email@email.com")
                                .nickname("bobby")
                                .pwd("hashed")
                                .photoId(UUID.randomUUID())
                                .isDeleted(false)
                                .build();

                UserEntity userAfter = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email("email@email.com")
                                .nickname("bobby")
                                .pwd("hashed")
                                .photoId(userBefore.getPhotoId())
                                .isDeleted(true)
                                .build();

                when(userRepository.findById(id)).thenReturn(Optional.of(userBefore));

                when(userRepository.save(any(UserEntity.class))).thenReturn(userAfter);

                userService.softDeleteUserById(id);

                when(userRepository.findById(id)).thenReturn(Optional.of(userAfter));

                UserEntity result = userService.getUserById(id, true);

                assertThat(result.getIsDeleted()).isTrue();
                verify(userRepository, times(2)).findById(id);
                verify(userRepository, times(1)).save(any(UserEntity.class));
        }

        @Test
        void deleteUserById_NotFound() {
                ObjectId id = new ObjectId();
                UserEntity userBefore = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email("email@email.com")
                                .nickname("bobby")
                                .pwd("hashed")
                                .photoId(UUID.randomUUID())
                                .isDeleted(false)
                                .build();

                UserEntity userAfter = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email("email@email.com")
                                .nickname("bobby")
                                .pwd("hashed")
                                .photoId(userBefore.getPhotoId())
                                .isDeleted(true)
                                .build();

                when(userRepository.findById(id)).thenReturn(Optional.of(userBefore));

                userService.softDeleteUserById(id);

                when(userRepository.findById(id)).thenReturn(Optional.of(userAfter));

                UserEntity result = userService.getUserById(id, true);

                assertThat(result.getIsDeleted()).isTrue();

                assertThrows(NotFoundException.class, () -> userService.softDeleteUserById(id));
                verify(userRepository, times(3)).findById(id);
                verify(userRepository, times(1)).save(any(UserEntity.class));
        }

        @Test
        void deleteUserByEmail_Success() {
                ObjectId id = new ObjectId();
                String email = "email@email.com";
                UserEntity userBefore = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email(email)
                                .nickname("bobby")
                                .pwd("hashed")
                                .photoId(UUID.randomUUID())
                                .isDeleted(false)
                                .build();

                UserEntity userAfter = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email(email)
                                .nickname("bobby")
                                .pwd("hashed")
                                .photoId(userBefore.getPhotoId())
                                .isDeleted(true)
                                .build();

                when(userRepository.findByEmail(email)).thenReturn(Optional.of(userBefore));

                when(userRepository.save(any(UserEntity.class))).thenReturn(userAfter);

                userService.softDeleteUserByEmail(email);

                when(userRepository.findByEmail(email)).thenReturn(Optional.of(userAfter));

                UserEntity result = userService.getUserByEmail(email, true);

                assertThat(result.getIsDeleted()).isTrue();
                verify(userRepository, times(2)).findById(id);
                verify(userRepository, times(1)).save(any(UserEntity.class));
        }

        @Test
        void deleteUserByEmail_NotFound() {
                ObjectId id = new ObjectId();
                String email = "email@email.com";
                UserEntity userBefore = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email(email)
                                .nickname("bobby")
                                .pwd("hashed")
                                .photoId(UUID.randomUUID())
                                .isDeleted(false)
                                .build();

                UserEntity userAfter = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email(email)
                                .nickname("bobby")
                                .pwd("hashed")
                                .photoId(userBefore.getPhotoId())
                                .isDeleted(true)
                                .build();

                when(userRepository.findByEmail(email)).thenReturn(Optional.of(userBefore));

                when(userRepository.save(any(UserEntity.class))).thenReturn(userAfter);

                userService.softDeleteUserByEmail(email);

                when(userRepository.findByEmail(email)).thenReturn(Optional.of(userAfter));

                UserEntity result = userService.getUserByEmail(email, true);

                assertThat(result.getIsDeleted()).isTrue();

                assertThrows(NotFoundException.class, () -> userService.softDeleteUserByEmail(email));

                verify(userRepository, times(3)).findByEmail(email);
                verify(userRepository, times(1)).save(any(UserEntity.class));
        }

        @Test
        void UpdateUserById_Success() {
                ObjectId id = new ObjectId();
                UserEntity userBefore = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email("email@email.com")
                                .nickname("bobby")
                                .pwd("hashed")
                                .photoId(UUID.randomUUID())
                                .isDeleted(false)
                                .build();

                UserEntity update = UserEntity.builder()
                                .email("newemail@email.com")
                                .nickname("lovesydneysweeney123")
                                .pwd("secret")
                                .build();

                UserEntity userAfter = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email("newemail@email.com")
                                .nickname("lovesydneysweeney123")
                                .pwd("secret")
                                .photoId(userBefore.getPhotoId())
                                .isDeleted(true)
                                .build();

                when(userRepository.findById(id)).thenReturn(Optional.of(userBefore));

                when(userRepository.save(any(UserEntity.class))).thenReturn(userAfter);

                UserEntity updated = userService.updateUserById(id, update);

                when(userRepository.findById(id)).thenReturn(Optional.of(userAfter));

                UserEntity result = userService.getUserById(id, false);

                assertThat(result.getName()).isEqualTo(updated.getName());
                assertThat(result.getNickname()).isEqualTo(updated.getNickname());
                assertThat(result.getEmail()).isEqualTo(updated.getEmail());
                assertThat(result.getPwd()).isEqualTo(updated.getPwd());

                verify(userRepository, times(2)).findById(id);
                verify(userRepository, times(1)).save(any(UserEntity.class));
        }

        @Test
        void updateUserById_NotFound_ThrowsNotFound() {
                ObjectId id = new ObjectId();
                UserEntity update = UserEntity.builder()
                                .email("new@example.com")
                                .nickname("newnick")
                                .pwd("newpwd")
                                .build();

                when(userRepository.findById(id)).thenReturn(Optional.empty());

                assertThrows(NotFoundException.class, () -> userService.updateUserById(id, update));

                verify(userRepository, times(1)).findById(id);
                verify(userRepository, never()).save(any());
        }

        @Test
        void updateUserByEmail_Success() {
                String email = "bob@example.com";
                ObjectId id = new ObjectId();
                UserEntity before = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email(email)
                                .nickname("bobby")
                                .pwd("hashed")
                                .photoId(UUID.randomUUID())
                                .isDeleted(false)
                                .build();

                UserEntity update = UserEntity.builder()
                                .email("new@example.com")
                                .nickname("newnick")
                                .pwd("newpwd")
                                .build();

                UserEntity after = UserEntity.builder()
                                .id(id)
                                .name("Bob")
                                .email("new@example.com")
                                .nickname("newnick")
                                .pwd("newpwd")
                                .photoId(before.getPhotoId())
                                .isDeleted(false)
                                .build();

                when(userRepository.findByEmail(email)).thenReturn(Optional.of(before));
                when(userRepository.save(any(UserEntity.class))).thenReturn(after);

                UserEntity result = userService.updateUserByEmail(email, update);

                assertThat(result.getEmail()).isEqualTo("new@example.com");
                assertThat(result.getNickname()).isEqualTo("newnick");
                assertThat(result.getPwd()).isEqualTo("newpwd");

                verify(userRepository, times(1)).findByEmail(email);
                verify(userRepository, times(1)).save(any());
        }

        @Test
        void updateUserByEmail_NotFound_ThrowsNotFound() {
                String email = "bob@example.com";
                UserEntity update = UserEntity.builder()
                                .email("new@example.com")
                                .nickname("newnick")
                                .pwd("newpwd")
                                .build();

                when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

                assertThrows(NotFoundException.class, () -> userService.updateUserByEmail(email, update));

                verify(userRepository, times(1)).findByEmail(email);
                verify(userRepository, never()).save(any());
        }
}
