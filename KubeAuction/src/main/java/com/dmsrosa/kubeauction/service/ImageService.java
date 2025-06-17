package com.dmsrosa.kubeauction.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;

@Service
public class ImageService {

    private final MinioClient minioClient;
    private final String bucketName = "images";

    public ImageService(@Value("${minio.uri}") String uri,
            @Value("${minio.user}") String user,
            @Value("${minio.key}") String key) {
        this.minioClient = MinioClient.builder()
                .endpoint(uri)
                .credentials(user, key)
                .build();
    }

    @PostConstruct
    public void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO bucket", e);
        }
    }

    public boolean imageExists(String filename) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error checking if image exists", e);
        }
    }

    public String saveImage(MultipartFile file, String filename) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return filename;
        } catch (Exception e) {
            throw new IOException("Failed to upload image to MinIO", e);
        }
    }

    public InputStream loadImage(String filename) throws IOException {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build());
        } catch (ErrorResponseException e) {
            throw new FileNotFoundException("Image not found in MinIO: " + filename);
        } catch (Exception e) {
            throw new IOException("Failed to load image from MinIO", e);
        }
    }
}
