package com.dmsrosa.kubeauction.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;

@Service
public class ImageService {

    private final MinioClient minioClient;
    private final String bucketName = "images";

    public ImageService() {
        this.minioClient = MinioClient.builder()
                .endpoint("http://minio:9000") // or your MinIO URL
                .credentials("minioadmin", "minioadmin")
                .build();

        try {
            // create bucket if not exists
            boolean found = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO bucket", e);
        }
    }

    public String saveImage(MultipartFile file, String filename) throws IOException {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return filename;
        } catch (MinioException e) {
            throw new IOException("Failed to upload to MinIO", e);
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public InputStream loadImage(String filename) throws IOException {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build());
        } catch (MinioException e) {
            throw new FileNotFoundException("File not found in MinIO: " + filename);
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
}
