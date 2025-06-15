package com.dmsrosa.kubeauction.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class ImageServiceTest {

    private ImageService imageService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("image-test-dir");
        imageService = new ImageService() {
            @Override
            public String saveImage(MultipartFile file, String filename) throws IOException {
                Path filePath = tempDir.resolve(filename);
                file.transferTo(filePath.toFile());
                return filePath.toString();
            }

            @Override
            public Resource loadImage(Path path, String filename) throws IOException {
                Path filePath = path.resolve(filename).normalize();
                if (!Files.exists(filePath)) {
                    throw new FileNotFoundException("File not found: " + filename);
                }
                return new UrlResource(filePath.toUri());
            }
        };
    }

    @Test
    void testSaveImage() throws IOException {
        byte[] content = "dummy image content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", content);

        imageService.saveImage(mockFile, "test.jpg");

        Path savedPath = tempDir.resolve("test.jpg");
        assertTrue(Files.exists(savedPath));
        assertEquals("dummy image content", Files.readString(savedPath));
    }

    @Test
    void testLoadImageSuccess() throws IOException {
        Path filePath = tempDir.resolve("load.jpg");
        Files.writeString(filePath, "loaded content");

        Resource resource = imageService.loadImage(tempDir, "load.jpg");

        assertTrue(resource.exists());
        assertEquals("load.jpg", resource.getFilename());
    }

    @Test
    void testLoadImageNotFound() {
        Exception exception = assertThrows(IOException.class, () -> imageService.loadImage(tempDir, "not_found.jpg"));

        assertTrue(exception.getMessage().contains("File not found"));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
                .map(Path::toFile)
                .sorted((a, b) -> -a.compareTo(b)) // delete children first
                .forEach(File::delete);
    }
}
