package com.humber.Tasky.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            return ResponseEntity.ok(Map.of("fileUrl", "/uploads/" + fileName));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "File upload failed"));
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(404).body("File not found");
            }

            byte[] fileContent = Files.readAllBytes(filePath);

            String contentDisposition = "attachment; filename=\"" + fileName + "\"";
            String htmlResponse = "<html><body>" +
                    "<script>window.close();</script>" +
                    "</body></html>";

            return ResponseEntity.ok()
                    .header("Content-Disposition", contentDisposition)
                    .header("Content-Type", "application/octet-stream")
                    .header("Content-Length", String.valueOf(fileContent.length))
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error downloading file: " + e.getMessage());
        }
    }
}
