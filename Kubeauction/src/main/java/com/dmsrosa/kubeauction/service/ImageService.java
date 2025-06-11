package com.dmsrosa.kubeauction.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

    private static final String IMAGE_DIR = "/app/uploads"; // mount point inside container

    public String saveImage(MultipartFile file, String filename) throws IOException {
        Path uploadPath = Paths.get(IMAGE_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        return filePath.toString();
    }

    public Resource loadImage(Path path, String filename) throws IOException {
        Path filePath = path.resolve(filename).normalize();
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + filename);
        }
        return new UrlResource(filePath.toUri());
    }
}