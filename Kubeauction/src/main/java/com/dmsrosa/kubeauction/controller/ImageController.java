package com.dmsrosa.kubeauction.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dmsrosa.kubeauction.service.ImageService;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    // TODO I ALWAYS EXPECT JPEG's, this probably should be dynamic and like should
    // change that

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file) {
        try {
            UUID uuid = UUID.randomUUID();
            String filename = uuid.toString() + ".jpeg";
            String savedFilename = imageService.saveImage(file, filename);
            return ResponseEntity.ok("Image uploaded successfully: " + savedFilename);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload image: " + e.getMessage());
        }
    }

    @GetMapping("/{filename}")
    public ResponseEntity<InputStreamResource> getImage(@PathVariable UUID uuid) {
        try {
            String filename = uuid.toString() + ".jpeg";
            var imageStream = imageService.loadImage(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(new InputStreamResource(imageStream));
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
