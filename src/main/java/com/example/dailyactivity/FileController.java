package com.example.dailyactivity;

import com.example.dailyactivity.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequestMapping("/files")
public class FileController {

    private final FileStorageService storage;

    public FileController(FileStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/{subdir}/{filename:.+}")
    public ResponseEntity<Resource> serve(
            @PathVariable String subdir,
            @PathVariable String filename
    ) {
        // Защита от path traversal
        if (!subdir.matches("[a-zA-Z0-9_-]+")
                || !filename.matches("[a-zA-Z0-9._-]+")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = storage.getRootPath()
                .resolve(subdir)
                .resolve(filename)
                .normalize();

        if (!filePath.startsWith(storage.getRootPath())) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CACHE_CONTROL,
                            "public, max-age=604800, immutable"
                    )
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}