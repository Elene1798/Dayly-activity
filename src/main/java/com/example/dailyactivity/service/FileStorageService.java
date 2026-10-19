package com.example.dailyactivity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp", "gif");

    @Value("${app.upload-dir:/app/uploads}")
    private String uploadDir;

    private Path rootPath;

    @PostConstruct
    public void init() {
        this.rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootPath.resolve("puzzles"));
            Files.createDirectories(rootPath.resolve("advent"));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удалось создать папки для загрузок", e
            );
        }
    }

    public String save(MultipartFile file, String subdir) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }

        String originalName = file.getOriginalFilename();
        String ext = extractExtension(originalName);

        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException(
                    "Недопустимый формат файла. Разрешены: " + ALLOWED_EXTENSIONS
            );
        }

        String fileName = UUID.randomUUID() + "." + ext;
        Path target = rootPath.resolve(subdir).resolve(fileName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл", e);
        }

        return fileName;
    }

    public void delete(String subdir, String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        try {
            Files.deleteIfExists(
                    rootPath.resolve(subdir).resolve(fileName)
            );
        } catch (IOException ignored) {}
    }

    public Path getRootPath() {
        return rootPath;
    }

    private String extractExtension(String name) {
        if (name == null) return null;
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return null;
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}