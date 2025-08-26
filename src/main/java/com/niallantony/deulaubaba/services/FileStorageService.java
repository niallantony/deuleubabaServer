package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.domain.HasImageSrc;
import com.niallantony.deulaubaba.domain.Student;
import lombok.extern.slf4j.Slf4j;
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
    public String storeImage(MultipartFile image) throws IOException {
        String filename = UUID.randomUUID() + "-" + image.getOriginalFilename();
        Path path = Paths.get("uploads", filename);
        Files.createDirectories(path.getParent());
        image.transferTo(path);
        return filename;
    }

    public void deleteImage(HasImageSrc data) {
        String oldAvatar = data.getImagesrc();
        if (oldAvatar != null && !oldAvatar.isEmpty()) {
            Path oldPath = Paths.get("uploads", oldAvatar);
            try {
                Files.deleteIfExists(oldPath);
            } catch (IOException e) {
                log.warn("Failed to delete old avatar: {}", oldPath, e);
            }
        }
    }
}
