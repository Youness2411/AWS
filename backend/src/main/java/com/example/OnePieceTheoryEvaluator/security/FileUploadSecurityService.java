package com.example.OnePieceTheoryEvaluator.security;

import com.example.OnePieceTheoryEvaluator.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class FileUploadSecurityService {

    // Allowed image MIME types
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg",
        "image/jpg", 
        "image/png",
        "image/gif",
        "image/webp"
    );

    // Allowed file extensions
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    // Maximum file size (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Image magic numbers (file signatures)
    private static final List<byte[]> IMAGE_SIGNATURES = Arrays.asList(
        new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}, // JPEG
        new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}, // PNG
        new byte[]{0x47, 0x49, 0x46, 0x38, 0x37, 0x61}, // GIF87a
        new byte[]{0x47, 0x49, 0x46, 0x38, 0x39, 0x61}, // GIF89a
        new byte[]{0x52, 0x49, 0x46, 0x46}, // WEBP (starts with RIFF)
        new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}, // JPEG
        new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1}  // JPEG
    );

    /**
     * Validates uploaded file for security
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("No file provided");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("File size exceeds maximum allowed size (5MB)");
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new ValidationException("Invalid file type. Only images are allowed");
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            throw new ValidationException("Invalid file extension. Only image files are allowed");
        }

        // Validate file content (magic numbers)
        try {
            validateFileContent(file);
        } catch (IOException e) {
            log.error("Error validating file content: {}", e.getMessage());
            throw new ValidationException("Unable to validate file content");
        }

        // Check for suspicious filename patterns
        if (containsSuspiciousPatterns(originalFilename)) {
            throw new ValidationException("Invalid filename");
        }
    }

    /**
     * Validates file content by checking magic numbers
     */
    private void validateFileContent(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[8];
            int bytesRead = inputStream.read(header);
            
            if (bytesRead < 3) {
                throw new ValidationException("File content is too short to be a valid image");
            }

            boolean isValidImage = false;
            for (byte[] signature : IMAGE_SIGNATURES) {
                if (startsWithSignature(header, signature)) {
                    isValidImage = true;
                    break;
                }
            }

            if (!isValidImage) {
                throw new ValidationException("File content does not match expected image format");
            }
        }
    }

    /**
     * Checks if file has valid extension
     */
    private boolean hasValidExtension(String filename) {
        String lowerFilename = filename.toLowerCase();
        return ALLOWED_EXTENSIONS.stream()
            .anyMatch(lowerFilename::endsWith);
    }

    /**
     * Checks if file starts with expected signature
     */
    private boolean startsWithSignature(byte[] fileHeader, byte[] signature) {
        if (fileHeader.length < signature.length) {
            return false;
        }
        
        for (int i = 0; i < signature.length; i++) {
            if (fileHeader[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks for suspicious filename patterns
     */
    private boolean containsSuspiciousPatterns(String filename) {
        String lowerFilename = filename.toLowerCase();
        
        // Check for path traversal attempts
        if (lowerFilename.contains("..") || lowerFilename.contains("/") || lowerFilename.contains("\\")) {
            return true;
        }
        
        // Check for script-like extensions
        String[] suspiciousExtensions = {".php", ".asp", ".jsp", ".exe", ".bat", ".sh", ".cmd"};
        for (String ext : suspiciousExtensions) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        
        // Check for suspicious characters
        String[] suspiciousChars = {"<", ">", "&", "|", ";", "`", "$", "(", ")", "{", "}"};
        for (String ch : suspiciousChars) {
            if (lowerFilename.contains(ch)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Sanitizes filename to prevent security issues
     */
    public String sanitizeFilename(String originalFilename) {
        if (originalFilename == null) {
            return "upload_" + System.currentTimeMillis();
        }
        
        // Remove path components
        String filename = originalFilename.replaceAll(".*[/\\\\]", "");
        
        // Remove suspicious characters
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Ensure it has an extension
        if (!filename.contains(".")) {
            filename += ".jpg";
        }
        
        // Limit length
        if (filename.length() > 100) {
            String name = filename.substring(0, 90);
            String ext = filename.substring(filename.lastIndexOf("."));
            filename = name + ext;
        }
        
        return filename;
    }
}
