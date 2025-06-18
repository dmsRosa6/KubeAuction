package com.dmsrosa.kubeauction.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
class ImageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUploadImage_thenDownloadImage() throws Exception {
        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/test-image.jpg"));

        MvcResult uploadResult = mockMvc.perform(multipart("/images/upload")
                .file("file", imageBytes))
                .andExpect(status().isOk())
                .andReturn();

        String imageId = uploadResult.getResponse().getContentAsString().replace("\"", "");

        mockMvc.perform(get("/images/" + imageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(imageBytes));
    }
}
