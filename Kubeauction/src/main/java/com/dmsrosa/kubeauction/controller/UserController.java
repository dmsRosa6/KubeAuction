package com.dmsrosa.kubeauction.controller;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmsrosa.kubeauction.database.dao.entity.UserEntity;
import com.dmsrosa.kubeauction.dto.user.CreateUserDto;
import com.dmsrosa.kubeauction.dto.user.UpdateUserDto;
import com.dmsrosa.kubeauction.dto.user.UserResponseDto;
import com.dmsrosa.kubeauction.service.AuctionService;
import com.dmsrosa.kubeauction.service.BidService;
import com.dmsrosa.kubeauction.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final BidService bidService;
    private final AuctionService auctionService;

    public UserController(UserService userService, BidService bidService, AuctionService auctionService) {
        this.userService = userService;
        this.bidService = bidService;
        this.auctionService = auctionService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody CreateUserDto user) {
        UserEntity created = userService.createUser(CreateUserDto.ToUserEntity(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.toUserResponseDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable ObjectId id) {
        return ResponseEntity.ok(UserResponseDto.toUserResponseDto(userService.getUserById(id, false)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUserById(
            @PathVariable ObjectId id,
            @RequestBody UpdateUserDto updates) {

        UserEntity updated = userService.updateUserById(id, UpdateUserDto.ToUserEntity(updates));
        return ResponseEntity.ok(UserResponseDto.toUserResponseDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable ObjectId id) {
        userService.softDeleteUserById(id);
        bidService.markUserDeletedByUserId(id);
        auctionService.markOwnerDeletedByOwnerId(id);
        return ResponseEntity.noContent().build();
    }

}
