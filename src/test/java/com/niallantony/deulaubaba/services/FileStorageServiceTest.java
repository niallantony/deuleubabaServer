package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.exceptions.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    private FileStorageService service;
    private Path tempDir;
    private final String FILES_FOLDER = "uploads";

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.tempDir = tempDir;
        service = new FileStorageService(tempDir.resolve(FILES_FOLDER).toString());
    }
    @Test
    void storeImage_success() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.png");

        String result = service.storeImage(file);
        Path imageDir = tempDir.resolve(FILES_FOLDER);
        Path expectedPath = Path.of(imageDir + "/" + result);

        assertTrue(result.endsWith("test.png"));
        verify(file).transferTo(expectedPath);
    }

    @Test
    void storeImage_failureWrapsInFileStorageException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("bad.png");
        doThrow(new IOException("disk full")).when(file).transferTo(any(Path.class));

        FileStorageException ex = assertThrows(FileStorageException.class,
                () -> service.storeImage(file));

        assertTrue(ex.getMessage().contains("bad.png"));
    }

    @Test
    void deleteImage_nullOrEmpty_doesNothing() {
        service.deleteImage(null);
        service.deleteImage("");
    }

    @Test
    void deleteImage_existingFile_deletesIt() throws IOException {

        Path imageDir = tempDir.resolve(FILES_FOLDER);
        Files.createDirectories(imageDir);
        Path filePath = imageDir.resolve("test.png");
        Files.createFile(filePath);

        assertTrue(Files.exists(filePath));

        service.deleteImage("test.png");
        assertFalse(Files.exists(filePath));
    }

    @Test
    void deleteImage_nonExistentFile_doesNothing() {
        service.deleteImage("does-not-exist.png");
    }
}