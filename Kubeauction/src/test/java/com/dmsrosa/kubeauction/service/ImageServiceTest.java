package com.dmsrosa.kubeauction.service;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Test
    public void loadImage_shouldReturnResource_whenFileExists() throws Exception {

        String filename = "test.txt";
        Path basePath = Paths.get("src/test/resources/uploads");
        Path testFilePath = basePath.resolve(filename);
        Files.createDirectories(basePath);
        Files.write(testFilePath, "dummy data".getBytes());

        Resource result = imageService.loadImage(basePath, filename);

        assertNotNull(result);
        assertTrue(result.exists());
        assertEquals(filename, result.getFilename());

        Files.deleteIfExists(testFilePath);
    }

    @Test
    public void loadImage_shouldThrow_whenFileDoesNotExist() {
        String filename = "nonexistent.png";
        Path basePath = Paths.get("src/test/resources/uploads");

        assertThrows(FileNotFoundException.class, () -> imageService.loadImage(basePath, filename));
    }
}
