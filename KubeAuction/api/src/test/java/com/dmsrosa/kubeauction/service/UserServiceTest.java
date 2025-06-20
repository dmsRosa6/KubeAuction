package com.dmsrosa.kubeauction.service;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dmsrosa.kubeauction.config.RedisConfig;
import com.dmsrosa.kubeauction.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.database.dao.repository.UserRepository;
import com.dmsrosa.kubeauction.exception.ConflictException;
import com.dmsrosa.kubeauction.exception.NotFoundException;
import com.dmsrosa.kubeauction.shared.database.domain.User;
import com.dmsrosa.kubeauction.shared.mapper.UserMapper;

class UserServiceTest {

        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private UserRepository userRepository;
        @Mock
        private RedisTemplate<String, Object> redisTemplate;
        @Mock
        private ValueOperations<String, Object> valueOps;

        @InjectMocks
        private UserService userService;

        private ObjectId userId;
        private User domainUser;
        private UserEntity entityUser;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                userId = new ObjectId();

                // Build a domain User and its corresponding entity
                domainUser = User.builder()
                                .id(userId)
                                .name("Alice")
                                .email("alice@example.com")
                                .nickname("ally")
                                .pwd("hashed")
                                .photoId(UUID.randomUUID())
                                .isDeleted(false)
                                .build();
                entityUser = UserMapper.toEntity(domainUser);

                when(redisTemplate.opsForValue()).thenReturn(valueOps);
        }

        @Test
        void createUser_savesAndReturnsDomain() {
                // given
                User toCreate = domainUser.toBuilder()
                                .id(null)
                                .pwd("rawpwd")
                                .build();
                UserEntity savedEntity = UserMapper.toEntity(toCreate).toBuilder()
                                .id(new ObjectId())
                                .pwd("encodedpwd")
                                .build();

                when(userRepository.existsByEmail(toCreate.getEmail())).thenReturn(false);
                when(passwordEncoder.encode("rawpwd")).thenReturn("encodedpwd");
                when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

                // when
                User result = userService.createUser(toCreate);

                // then
                assertThat(result.getId()).isEqualTo(savedEntity.getId());
                assertThat(result.getEmail()).isEqualTo(toCreate.getEmail());
                assertThat(result.getPwd()).isEqualTo("encodedpwd");
                assertThat(result.isDeleted()).isFalse();

                verify(userRepository).existsByEmail(toCreate.getEmail());
                verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        void createUser_duplicateEmail_throwsConflict() {
                // given
                when(userRepository.existsByEmail(domainUser.getEmail())).thenReturn(true);

                // when / then
                assertThatThrownBy(() -> userService.createUser(domainUser))
                                .isInstanceOf(ConflictException.class)
                                .hasMessageContaining("User already exists");

                verify(userRepository).existsByEmail(domainUser.getEmail());
                verify(userRepository, never()).save(any());
        }

        @Test
        void getUserById_found_returnsDomain() {
                when(valueOps.get(RedisConfig.USERS_PREFIX_DELIM + userId)).thenReturn(null);
                when(userRepository.findById(userId)).thenReturn(Optional.of(entityUser));

                User result = userService.getUserById(userId, false);

                assertThat(result).isEqualTo(UserMapper.toDomain(entityUser));
                verify(userRepository).findById(userId);
                verify(valueOps).set(RedisConfig.USERS_PREFIX_DELIM + userId, result);
        }

        @Test
        void getUserById_notFound_throws() {
                when(valueOps.get(anyString())).thenReturn(null);
                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.getUserById(userId, false))
                                .isInstanceOf(NotFoundException.class);

                verify(userRepository).findById(userId);
        }

        @Test
        void getUserByEmail_found_returnsDomain() {
                when(userRepository.findByEmail(domainUser.getEmail())).thenReturn(Optional.of(entityUser));

                User result = userService.getUserByEmail(domainUser.getEmail(), false);

                assertThat(result).isEqualTo(UserMapper.toDomain(entityUser));
                verify(userRepository).findByEmail(domainUser.getEmail());
        }

        @Test
        void getUserByEmail_notFound_throws() {
                when(userRepository.findByEmail(domainUser.getEmail())).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.getUserByEmail(domainUser.getEmail(), false))
                                .isInstanceOf(NotFoundException.class);

                verify(userRepository).findByEmail(domainUser.getEmail());
        }

        @Test
        void softDeleteUserById_marksDeletedAndAllowsGetWhenAllowed() {
                // soft-delete
                when(userRepository.findById(userId)).thenReturn(Optional.of(entityUser));
                when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

                userService.softDeleteUserById(userId);
                verify(userRepository).save(argThat(e -> e.getIsDeleted()));

                // get with includeDeleted=true
                UserEntity deletedEntity = entityUser.toBuilder().isDeleted(true).build();
                when(userRepository.findById(userId)).thenReturn(Optional.of(deletedEntity));

                User result = userService.getUserById(userId, true);
                assertThat(result.isDeleted()).isTrue();
        }

        @Test
        void softDeleteUserById_notFound_throws() {
                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.softDeleteUserById(userId))
                                .isInstanceOf(NotFoundException.class);

                verify(userRepository).findById(userId);
        }

        @Test
        void softDeleteUserByEmail_marksDeletedAndAllowsGetWhenAllowed() {
                String email = domainUser.getEmail();

                when(userRepository.findByEmail(email)).thenReturn(Optional.of(entityUser));
                when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

                userService.softDeleteUserByEmail(email);
                verify(userRepository).save(argThat(e -> e.getIsDeleted()));

                UserEntity deletedEntity = entityUser.toBuilder().isDeleted(true).build();
                when(userRepository.findByEmail(email)).thenReturn(Optional.of(deletedEntity));

                User result = userService.getUserByEmail(email, true);
                assertThat(result.isDeleted()).isTrue();
        }

        @Test
        void softDeleteUserByEmail_notFound_throws() {
                when(userRepository.findByEmail(domainUser.getEmail())).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.softDeleteUserByEmail(domainUser.getEmail()))
                                .isInstanceOf(NotFoundException.class);

                verify(userRepository).findByEmail(domainUser.getEmail());
        }

        @Test
        void updateUserById_success_updatesAndEncodesPwd() {
                UserEntity before = entityUser;
                User toUpdate = User.builder()
                                .nickname("newnick")
                                .pwd("newraw")
                                .build();
                UserEntity afterSave = before.toBuilder()
                                .nickname("newnick")
                                .pwd("encodednew")
                                .build();

                when(userRepository.findById(userId)).thenReturn(Optional.of(before));
                when(passwordEncoder.encode("newraw")).thenReturn("encodednew");
                when(userRepository.save(any())).thenReturn(afterSave);

                User result = userService.updateUserById(userId, toUpdate);
                assertThat(result.getNickname()).isEqualTo("newnick");
                assertThat(result.getPwd()).isEqualTo("encodednew");

                verify(userRepository).findById(userId);
                verify(userRepository).save(any());
        }

        @Test
        void updateUserById_notFound_throws() {
                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.updateUserById(userId, User.builder().build()))
                                .isInstanceOf(NotFoundException.class);

                verify(userRepository).findById(userId);
        }

        @Test
        void updateUserByEmail_success_updatesAndEncodesPwd() {
                String email = domainUser.getEmail();
                UserEntity before = entityUser;
                User toUpdate = User.builder()
                                .email("new@example.com")
                                .nickname("nn")
                                .pwd("rawpwd")
                                .build();
                UserEntity afterSave = before.toBuilder()
                                .email("new@example.com")
                                .nickname("nn")
                                .pwd("encodedpwd")
                                .build();

                when(userRepository.findByEmail(email)).thenReturn(Optional.of(before));
                when(passwordEncoder.encode("rawpwd")).thenReturn("encodedpwd");
                when(userRepository.save(any())).thenReturn(afterSave);

                User result = userService.updateUserByEmail(email, toUpdate);
                assertThat(result.getEmail()).isEqualTo("new@example.com");
                assertThat(result.getPwd()).isEqualTo("encodedpwd");

                verify(userRepository).findByEmail(email);
                verify(userRepository).save(any());
        }

        @Test
        void updateUserByEmail_notFound_throws() {
                when(userRepository.findByEmail(domainUser.getEmail())).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.updateUserByEmail(domainUser.getEmail(), User.builder().build()))
                                .isInstanceOf(NotFoundException.class);

                verify(userRepository).findByEmail(domainUser.getEmail());
        }

        @Test
        void getUserById_cacheHit_returnsFromCache() {
                String key = RedisConfig.USERS_PREFIX_DELIM + userId;
                when(valueOps.get(key)).thenReturn(domainUser);

                User result = userService.getUserById(userId, false);
                assertThat(result).isSameAs(domainUser);
                verify(userRepository, never()).findById(any());
        }

        @Test
        void getUserById_cacheMiss_thenStores() {
                String key = RedisConfig.USERS_PREFIX_DELIM + userId;
                when(valueOps.get(key)).thenReturn(null);
                when(userRepository.findById(userId)).thenReturn(Optional.of(entityUser));

                User result = userService.getUserById(userId, false);
                assertThat(result).isEqualTo(UserMapper.toDomain(entityUser));
                verify(valueOps).set(key, result);
        }
}
