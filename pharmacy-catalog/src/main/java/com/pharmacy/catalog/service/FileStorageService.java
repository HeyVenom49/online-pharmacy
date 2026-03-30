package com.pharmacy.catalog.service;

import com.pharmacy.common.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload.path:./uploads/prescriptions}")
    private String uploadPath;

    public String storeFile(MultipartFile file) {
        try {
            FileUtils.validateImageFile(file);

            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String fileName = FileUtils.generateFileName(file.getOriginalFilename());
            Path targetLocation = uploadDir.resolve(fileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file: {}", fileName);
            return fileName;

        } catch (Exception e) {
            log.error("Could not store file", e);
            throw new com.pharmacy.common.exception.InvalidFileException("Could not store file: " + e.getMessage());
        }
    }

    public byte[] downloadFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadPath).resolve(fileName);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Could not read file: {}", fileName, e);
            throw new com.pharmacy.common.exception.ResourceNotFoundException("File not found: " + fileName);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadPath).resolve(fileName);
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", fileName);
        } catch (IOException e) {
            log.error("Could not delete file: {}", fileName, e);
        }
    }
}
