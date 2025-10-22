package com.example.OnePieceTheoryEvaluator.controller;

import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.security.FileUploadSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final FileUploadSecurityService fileUploadSecurityService;
    
    @Value("${app.images.dir}")
    private String imageDirectory;
    
    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> uploadImage(@RequestParam("image") MultipartFile imageFile) {
        try {
            // Validate file security
            fileUploadSecurityService.validateFile(imageFile);
            
            // Create directory if it doesn't exist
            File directory = new File(imageDirectory);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                log.info("Directory created: {} (success: {})", imageDirectory, created);
            } else {
                log.info("Directory already exists: {}", imageDirectory);
            }
            
            // Sanitize filename and generate unique name
            String sanitizedFilename = fileUploadSecurityService.sanitizeFilename(imageFile.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID() + "_" + sanitizedFilename;
            
            // Save file
            String imagePath = imageDirectory + (imageDirectory.endsWith("/") ? "" : "/") + uniqueFileName;
            File destinationFile = new File(imagePath);
            imageFile.transferTo(destinationFile);
            
            String imageUrl = baseUrl + "/api/uploads/image/" + uniqueFileName;
            
            log.info("Image uploaded successfully: {}", uniqueFileName);
            
            return ResponseEntity.ok(Response.builder()
                    .status(200)
                    .message("Image uploaded successfully")
                    .url(imageUrl)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error uploading image: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Response.builder()
                    .status(400)
                    .message("Failed to upload image: " + e.getMessage())
                    .build());
        }
    }
    
    @GetMapping("/image/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            String imagePath = imageDirectory + (imageDirectory.endsWith("/") ? "" : "/") + filename;
            File imageFile = new File(imagePath);
            
            if (!imageFile.exists()) {
                log.warn("Image not found: {}", imagePath);
                return ResponseEntity.notFound().build();
            }
            
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String contentType = Files.probeContentType(imageFile.toPath());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                    .body(imageBytes);
                    
        } catch (IOException e) {
            log.error("Error reading image: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}