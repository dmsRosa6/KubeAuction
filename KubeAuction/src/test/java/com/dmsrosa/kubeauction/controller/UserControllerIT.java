package com.dmsrosa.kubeauction.controller;

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

import com.dmsrosa.kubeauction.dto.user.CreateUserDto;
import com.dmsrosa.kubeauction.dto.user.UserResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper mapper;

        @Test
        void fullUserCrudFlow() throws Exception {
                CreateUserDto create = new CreateUserDto();
                create.setName("Jane");
                create.setEmail("jane@example.com");
                create.setPwd("p@ssw0rd");

                String createJson = mapper.writeValueAsString(create);

                MvcResult mvcResult = mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.name").value("Jane"))
                                .andExpect(jsonPath("$.email").value("jane@example.com"))
                                .andReturn();

                String responseBody = mvcResult.getResponse().getContentAsString();
                UserResponseDto user = mapper.readValue(responseBody, UserResponseDto.class);
                String id = user.getId();

                user.setNickname("janie");
                String updateJson = mapper.writeValueAsString(user);

                mockMvc.perform(put("/api/users/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nickname").value("janie"));

                mockMvc.perform(delete("/api/users/{id}", id))
                                .andExpect(status().isNoContent());

                mockMvc.perform(get("/api/users/{id}", id))
                                .andExpect(status().isNotFound());
        }
}
