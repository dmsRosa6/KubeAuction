package com.dmsrosa.kubeauction.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dmsrosa.kubeauction.dto.auction.AuctionResponseDto;
import com.dmsrosa.kubeauction.dto.user.CreateUserDto;
import com.dmsrosa.kubeauction.dto.user.UserResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuctionControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    @Test
    void fullAuctionCrudFlow() throws Exception {
        CreateUserDto createUser = new CreateUserDto();
        createUser.setName("Alice");
        createUser.setEmail("alice@example.com");
        createUser.setPwd("secret");
        String userJson = mapper.writeValueAsString(createUser);
        String userBody = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserResponseDto user = mapper.readValue(userBody, UserResponseDto.class);
        ObjectId userId = new ObjectId(user.getId());

        Map<String, Object> auction = new HashMap<>();
        auction.put("title", "Spring Auction");
        auction.put("descr", "Integration test auction");
        auction.put("imageId", UUID.randomUUID());
        auction.put("ownerId", userId.toString());
        auction.put("endDate", Instant.now().plusSeconds(3600).toString());
        auction.put("minimumPrice", 50.0);
        String auctionJson = mapper.writeValueAsString(auction);

        MvcResult mvcResult = mockMvc.perform(post("/api/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(auctionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Spring Auction"))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        AuctionResponseDto created = mapper.readValue(responseBody, AuctionResponseDto.class);
        String auctionId = created.getId();

        mockMvc.perform(get("/api/auctions/{id}", auctionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descr").value("Integration test auction"));

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Updated Auction");
        String updateJson = mapper.writeValueAsString(updates);

        mockMvc.perform(put("/api/auctions/{id}", auctionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Auction"));

        mockMvc.perform(delete("/api/auctions/{id}", auctionId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auctions/{id}", auctionId))
                .andExpect(status().isNotFound());
    }
}
