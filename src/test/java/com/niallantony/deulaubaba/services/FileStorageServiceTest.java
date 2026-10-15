package com.niallantony.deulaubaba.services;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.niallantony.deulaubaba.domain.HasImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private FileStorageService fileStorageService;

    private final MockMultipartFile image = new MockMultipartFile("image", "new.jpg", "image/jpeg", "image".getBytes());

    private static class TestEntity extends HasImage {
        private String image;

        TestEntity(String image) {
            this.image = image;
        }

        @Override
        public String getImage() {
            return image;
        }

        @Override
        public String getBucketId() {
            return "bucket";
        }

        @Override
        public void setImage(String image) {
            this.image = image;
        }
    }

    @BeforeEach
    void startTransaction() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void endTransaction() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private void stubUpload() {
        when(storage.get("bucket")).thenReturn(mock(Bucket.class));
        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(mock(Blob.class));
    }

    @Test
    void swapImage_setsNewImageWithoutDeletingOldImageBeforeCommit() {
        stubUpload();
        TestEntity entity = new TestEntity("old.jpg");

        fileStorageService.swapImage(image, entity);

        assertNotEquals("old.jpg", entity.getImage());
        assertTrue(entity.getImage().endsWith("new.jpg"));
        verify(storage, never()).delete(any(BlobId.class));
    }

    @Test
    void swapImage_deletesOldImageAfterCommit() {
        stubUpload();
        Blob oldBlob = mock(Blob.class);
        BlobId oldBlobId = BlobId.of("bucket", "old.jpg");
        when(oldBlob.getBlobId()).thenReturn(oldBlobId);
        when(storage.get("bucket", "old.jpg")).thenReturn(oldBlob);

        fileStorageService.swapImage(image, new TestEntity("old.jpg"));
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        verify(storage).delete(eq(oldBlobId));
    }

    @Test
    void swapImage_keepsOldImageWhenTransactionRollsBack() {
        stubUpload();

        fileStorageService.swapImage(image, new TestEntity("old.jpg"));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(storage, never()).delete(any(BlobId.class));
    }

    @Test
    void swapImage_withNoPreviousImage_doesNotScheduleDeletion() {
        stubUpload();
        TransactionSynchronizationManager.clearSynchronization();

        fileStorageService.swapImage(image, new TestEntity(null));

        verify(storage, never()).delete(any(BlobId.class));
    }

    @Test
    void swapImage_replacingImageOutsideTransaction_throws() {
        stubUpload();
        TransactionSynchronizationManager.clearSynchronization();

        assertThrows(
                IllegalStateException.class,
                () -> fileStorageService.swapImage(image, new TestEntity("old.jpg"))
        );
    }

    @Test
    void swapImage_whenUploadFails_keepsOldImage() {
        when(storage.get("bucket")).thenReturn(mock(Bucket.class));
        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenThrow(new StorageException(500, "fail"));
        TestEntity entity = new TestEntity("old.jpg");

        assertDoesNotThrow(() -> fileStorageService.swapImage(image, entity));

        assertEquals("old.jpg", entity.getImage());
        assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());
    }

    @Test
    void swapImage_whenBucketLookupFails_keepsOldImage() {
        when(storage.get("bucket")).thenThrow(new StorageException(500, "fail"));
        TestEntity entity = new TestEntity("old.jpg");

        assertDoesNotThrow(() -> fileStorageService.swapImage(image, entity));

        assertEquals("old.jpg", entity.getImage());
    }

    @Test
    void deleteImageAfterCommit_whenDeleteFails_doesNotThrow() {
        when(storage.get("bucket", "old.jpg")).thenThrow(new StorageException(500, "fail"));

        fileStorageService.deleteImageAfterCommit("old.jpg", "bucket");

        assertDoesNotThrow(() -> TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit));
    }

    @Test
    void deleteImageAfterCommit_withNoImage_doesNothing() {
        TransactionSynchronizationManager.clearSynchronization();

        assertDoesNotThrow(() -> fileStorageService.deleteImageAfterCommit(null, "bucket"));
        verifyNoInteractions(storage);
    }
}
