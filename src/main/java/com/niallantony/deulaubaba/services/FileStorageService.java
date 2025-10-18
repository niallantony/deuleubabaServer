package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.domain.HasImage;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {
    private final Path uploadDirectory;

    public FileStorageService(@Value("${upload.dir:uploads}") String uploadDirectory) {
        this.uploadDirectory = Paths.get(uploadDirectory);
    }

    public String storeImage(MultipartFile image) throws FileStorageException {
        try {
            String filename = UUID.randomUUID() + "-" + image.getOriginalFilename();
            Path path = uploadDirectory.resolve(filename);
            Files.createDirectories(path.getParent());
            image.transferTo(path);
            return filename;
        } catch (IOException e) {
            throw new FileStorageException("Could not store image" + image.getOriginalFilename(), e);
        }
    }

    public void deleteImage(String oldAvatar) {
        if (oldAvatar != null && !oldAvatar.isEmpty()) {
            Path oldPath = uploadDirectory.resolve(oldAvatar);
            try {
                Files.deleteIfExists(oldPath);
            } catch (IOException e) {
                throw new FileStorageException("Could not delete image" + oldAvatar, e);
            }
        }
    }

    public void swapImage(MultipartFile image, HasImage entity) {
        if (image == null || entity == null || image.isEmpty()) {
            return;
        }
        String oldUri = entity.getImage();
        String newUri;
        try {
            newUri = storeImage(image);
        } catch (FileStorageException e) {
            log.warn("Could not store image " + oldUri, e);
            return;
        }
        if (oldUri != null && !oldUri.isEmpty()) {
           try {
               deleteImage(oldUri);
           } catch (FileStorageException e) {
               log.warn("Could not delete image{}", oldUri, e);
           }
        }
        if (newUri != null && !newUri.isEmpty()) {
            entity.setImage(newUri);
        }
    }

}
