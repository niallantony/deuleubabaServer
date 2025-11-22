package com.niallantony.deulaubaba.services;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.niallantony.deulaubaba.domain.HasImage;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FileStorageService {
    private final Path uploadDirectory;
    private static final Storage storage;

    static {
        try (FileInputStream serviceAccountFile = new FileInputStream(System.getenv("GOOGLE_APPLICATION_CREDENTIAL"))) {
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountFile);
            storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId("deulaubaba").build().getService();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load GOOGLE_APPLICATION_CREDENTIAL", e);
        }
    }

    public FileStorageService(@Value("${upload.dir:uploads}") String uploadDirectory) {
        this.uploadDirectory = Paths.get(uploadDirectory);
    }

    public String generateSignedURL(HasImage entity) throws FileStorageException {
        if (entity == null || entity.getImage() == null) return "";
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(entity.getBucketId(), entity.getImage())).build();
        return storage.signUrl(blobInfo, 15, TimeUnit.MINUTES).toString();
    }


    // TODO: Refactor tests
    public void deleteImage(String oldId, String bucketName) {
        try {
            Blob blob = storage.get(bucketName, oldId);
            if (blob != null) {
                BlobId blobId = blob.getBlobId();
                storage.delete(blobId);
            }
        } catch (StorageException e) {
            throw new FileStorageException("Could not delete image" + oldId, e);
        }
    }

    public String storeImageGoogleCloud(MultipartFile image, String bucketname) throws FileStorageException {
        try {
            String filename = UUID.randomUUID() + "-" + image.getOriginalFilename();
            BlobId blobId = BlobId.of(bucketname, filename);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpeg").build();
            storage.create(blobInfo, image.getBytes());
            return filename;
        } catch (IOException e) {
            throw new FileStorageException("Could not store image" + image.getOriginalFilename(), e);
        }
    }

    public void getBucketOrCreate(String bucketName ) throws FileStorageException {
        Bucket bucket = storage.get(bucketName);
        if (bucket == null) {
            try {
                createBucket(bucketName);
            } catch (FileStorageException e) {
                throw new FileStorageException("Could not create bucket: " + bucketName, e);
            }
        }
    }

    public void createBucket(String bucketName) {
        try {
            storage.create(BucketInfo.newBuilder(bucketName).setLocation("ASIA-NORTHEAST3").build());
        } catch (StorageException e) {
            throw new FileStorageException("Could not create bucket " + bucketName, e);

        }
    }

    public void swapImage(MultipartFile image, HasImage entity) {
        if (image == null || entity == null || image.isEmpty()) {
            return;
        }
        String oldId = entity.getImage();
        String bucketId = entity.getBucketId();
        String newId;
        try {
            getBucketOrCreate(bucketId);
            newId = storeImageGoogleCloud(image, bucketId);
        } catch (FileStorageException e) {
            log.warn("Could not store image " + oldId, e);
            return;
        }
        if (oldId != null && !oldId.isEmpty()) {
           try {
               deleteImage(oldId, entity.getBucketId());
           } catch (FileStorageException e) {
               log.warn("Could not delete image{}", oldId, e);
           }
        }
        if (newId != null && !newId.isEmpty()) {
            entity.setImage(newId);
        }
    }

}
