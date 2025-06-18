// src/test/java/com/dmsrosa/kubeauction/controller/BidControllerIT.java

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dmsrosa.kubeauction.dto.auction.AuctionResponseDto;
import com.dmsrosa.kubeauction.dto.bid.BidResponseDto;
import com.dmsrosa.kubeauction.dto.user.CreateUserDto;
import com.dmsrosa.kubeauction.dto.user.UserResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class BidControllerIT {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        ObjectMapper mapper;

        @Test
        void fullBidCrudFlow() throws Exception {
                CreateUserDto createUser = new CreateUserDto();
                createUser.setName("Bob");
                createUser.setEmail("bob@example.com");
                createUser.setPwd("password");
                String userJson = mapper.writeValueAsString(createUser);

                String userBody = mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userJson))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();
                UserResponseDto user = mapper.readValue(userBody, UserResponseDto.class);
                String userId = user.getId();
                ObjectId uoid = new ObjectId(userId);

                Map<String, Object> auction = new HashMap<>();
                auction.put("title", "Bid-Flow Auction");
                auction.put("descr", "for bid test");
                auction.put("imageId", UUID.randomUUID());
                auction.put("ownerId", uoid.toString());
                auction.put("endDate", Instant.now().plusSeconds(3600).toString());
                auction.put("minimumPrice", 25.0);
                String auctionJson = mapper.writeValueAsString(auction);

                String auctionBody = mockMvc.perform(post("/api/auctions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(auctionJson))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();
                AuctionResponseDto auctionResp = mapper.readValue(auctionBody, AuctionResponseDto.class);
                String auctionId = auctionResp.getId();

                Map<String, Object> bidReq = new HashMap<>();
                bidReq.put("userId", userId);
                bidReq.put("auctionId", auctionId);
                bidReq.put("value", 123);
                String bidJson = mapper.writeValueAsString(bidReq);

                MvcResult bidResult = mockMvc.perform(post("/api/bids")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bidJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.value").value(123))
                                .andReturn();

                BidResponseDto bidResp = mapper.readValue(
                                bidResult.getResponse().getContentAsString(),
                                BidResponseDto.class);
                String bidId = bidResp.getId();

                mockMvc.perform(get("/api/bids/{id}", bidId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.userId").value(userId))
                                .andExpect(jsonPath("$.auctionId").value(auctionId))
                                .andExpect(jsonPath("$.value").value(123));

                mockMvc.perform(delete("/api/bids/{id}", bidId))
                                .andExpect(status().isNoContent());

                mockMvc.perform(get("/api/bids/{id}", bidId))
                                .andExpect(status().isNotFound());
        }
}
