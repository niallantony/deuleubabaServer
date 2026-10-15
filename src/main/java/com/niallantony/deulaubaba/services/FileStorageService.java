package com.niallantony.deulaubaba.services;

import com.google.cloud.storage.*;
import com.niallantony.deulaubaba.domain.HasImage;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FileStorageService {
    private final Storage storage;


    public FileStorageService(Storage storage) {
        this.storage = storage;
    }

    public String generateSignedURL(HasImage entity) throws FileStorageException {
        if (entity == null || entity.getImage() == null) return "";
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(entity.getBucketId(), entity.getImage())).build();
        return storage.signUrl(blobInfo, 15, TimeUnit.MINUTES).toString();
    }


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
        } catch (IOException | StorageException e) {
            throw new FileStorageException("Could not store image" + image.getOriginalFilename(), e);
        }
    }

    public void getBucketOrCreate(String bucketName ) throws FileStorageException {
        Bucket bucket;
        try {
            bucket = storage.get(bucketName);
        } catch (StorageException e) {
            throw new FileStorageException("Could not get bucket: " + bucketName, e);
        }
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

    /**
     * Uploads a replacement image and points the entity at it. The old image is
     * only deleted once the surrounding transaction commits: storage can't take
     * part in the database transaction, so deleting it straight away would leave
     * the entity pointing at a missing image whenever the save fails or rolls back.
     */
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
        if (newId != null && !newId.isEmpty()) {
            entity.setImage(newId);
        }
        deleteImageAfterCommit(oldId, bucketId);
    }

    /**
     * Deletes an image once the surrounding transaction commits, so a failed or
     * rolled-back database change never leaves an entity pointing at a deleted
     * image. Failures to delete are logged rather than thrown, since the database
     * change has already been committed by then.
     */
    public void deleteImageAfterCommit(String imageId, String bucketId) {
        if (imageId == null || imageId.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Replacing an image must happen inside a transaction");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    deleteImage(imageId, bucketId);
                } catch (FileStorageException e) {
                    log.warn("Could not delete image {}", imageId, e);
                }
            }
        });
    }

}
