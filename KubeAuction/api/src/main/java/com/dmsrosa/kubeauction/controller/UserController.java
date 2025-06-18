package com.dmsrosa.kubeauction.controller;

import java.net.URI;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmsrosa.kubeauction.dto.user.CreateUserDto;
import com.dmsrosa.kubeauction.dto.user.UpdateUserDto;
import com.dmsrosa.kubeauction.dto.user.UserResponseDto;
import com.dmsrosa.kubeauction.service.AuctionService;
import com.dmsrosa.kubeauction.service.BidService;
import com.dmsrosa.kubeauction.service.UserService;
import com.dmsrosa.kubeauction.shared.database.domain.User;

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
        User created = userService.createUser(CreateUserDto.ToUserEntity(user));
        URI location = URI.create("/api/users/" + created.getId());

        return ResponseEntity
                .created(location)
                .body(UserResponseDto.toUserResponseDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        return ResponseEntity.ok(UserResponseDto.toUserResponseDto(userService.getUserById(oid, false)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUserById(
            @PathVariable String id,
            @RequestBody UpdateUserDto updates) {
        ObjectId oid = new ObjectId(id);
        User updated = userService.updateUserById(oid, UpdateUserDto.ToUserEntity(updates));
        return ResponseEntity.ok(UserResponseDto.toUserResponseDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable String id) {
        ObjectId oid = new ObjectId(id);
        userService.softDeleteUserById(oid);
        bidService.markUserDeletedByUserId(oid);
        auctionService.markOwnerDeletedByOwnerId(oid);
        return ResponseEntity.noContent().build();
    }

}
