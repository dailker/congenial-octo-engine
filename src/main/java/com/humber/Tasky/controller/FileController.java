package com.humber.Tasky.controller;

import com.humber.Tasky.model.File;
import com.humber.Tasky.repository.FileRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.humber.Tasky.model.Team.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
@RestController
@Tag(name = "File", description = "File API")
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileRepository fileRepository;

    @Operation(summary = "Upload a file", description = "Upload a file and return the file ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "File upload failed")
    })
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("uploaderId") String uploaderId) {
        try {
            // Generate a unique filename and store the file
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dbFile = new File(fileName, file.getContentType(), file.getBytes(), uploaderId);
            File savedFile = fileRepository.save(dbFile);

            // Return the file ID so it can be used in the chat message
            return ResponseEntity.ok(Map.of("fileId", savedFile.getId(),
                                            "fileName", savedFile.getName(),
                                            "fileType", savedFile.getContentType()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "File upload failed"));
        }
    }

    @Operation(summary = "Download a file", description = "Download a file by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable String id) {
        Optional<File> optionalFile = fileRepository.findById(id);
        if (optionalFile.isEmpty()) {
            return ResponseEntity.status(404).body("File not found");
        }

        File file = optionalFile.get();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .header("Content-Type", file.getContentType())
                .header("Content-Length", String.valueOf(file.getContent().length))
                .body(file.getContent());
    }
}
