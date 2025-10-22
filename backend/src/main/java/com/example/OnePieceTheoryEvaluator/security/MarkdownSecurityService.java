package com.example.OnePieceTheoryEvaluator.security;

import com.example.OnePieceTheoryEvaluator.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
@Slf4j
public class MarkdownSecurityService {

    // Patterns for potentially dangerous content
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
        "(?i)<\\s*script[^>]*>.*?</\\s*script\\s*>", 
        Pattern.DOTALL
    );
    
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
        "(?i)javascript\\s*:", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern ONLOAD_PATTERN = Pattern.compile(
        "(?i)on\\w+\\s*=", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern IFRAME_PATTERN = Pattern.compile(
        "(?i)<\\s*iframe[^>]*>.*?</\\s*iframe\\s*>", 
        Pattern.DOTALL
    );
    
    private static final Pattern OBJECT_PATTERN = Pattern.compile(
        "(?i)<\\s*object[^>]*>.*?</\\s*object\\s*>", 
        Pattern.DOTALL
    );
    
    private static final Pattern EMBED_PATTERN = Pattern.compile(
        "(?i)<\\s*embed[^>]*>", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern FORM_PATTERN = Pattern.compile(
        "(?i)<\\s*form[^>]*>.*?</\\s*form\\s*>", 
        Pattern.DOTALL
    );
    
    
    private static final Pattern DATA_PATTERN = Pattern.compile(
        "(?i)data\\s*:", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile(
        "(?i)vbscript\\s*:", 
        Pattern.CASE_INSENSITIVE
    );

    // Maximum content length
    private static final int MAX_CONTENT_LENGTH = 50000; // 50KB

    /**
     * Sanitizes markdown content to prevent XSS and other security issues
     */
    public String sanitizeMarkdown(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        // Check content length
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ValidationException("Content exceeds maximum allowed length");
        }

        String sanitized = content;

        // Remove dangerous HTML tags and attributes
        sanitized = removeDangerousContent(sanitized);
        
        // Remove dangerous links
        sanitized = removeDangerousLinks(sanitized);
        
        // Remove dangerous attributes
        sanitized = removeDangerousAttributes(sanitized);

        log.info("Markdown content sanitized. Original length: {}, Sanitized length: {}", 
                content.length(), sanitized.length());

        return sanitized;
    }

    /**
     * Validates markdown content for security issues
     */
    public void validateMarkdown(String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }

        // Check for script tags
        if (SCRIPT_PATTERN.matcher(content).find()) {
            throw new ValidationException("Script tags are not allowed in content");
        }

        // Check for javascript: URLs
        if (JAVASCRIPT_PATTERN.matcher(content).find()) {
            throw new ValidationException("JavaScript URLs are not allowed");
        }

        // Check for event handlers
        if (ONLOAD_PATTERN.matcher(content).find()) {
            throw new ValidationException("Event handlers are not allowed in content");
        }

        // Check for iframes
        if (IFRAME_PATTERN.matcher(content).find()) {
            throw new ValidationException("Iframe tags are not allowed in content");
        }

        // Check for object/embed tags
        if (OBJECT_PATTERN.matcher(content).find() || EMBED_PATTERN.matcher(content).find()) {
            throw new ValidationException("Object/embed tags are not allowed in content");
        }

        // Check for forms
        if (FORM_PATTERN.matcher(content).find()) {
            throw new ValidationException("Form tags are not allowed in content");
        }

        // Check for dangerous data URLs
        if (DATA_PATTERN.matcher(content).find()) {
            throw new ValidationException("Data URLs are not allowed in content");
        }

        // Check for vbscript
        if (VBSCRIPT_PATTERN.matcher(content).find()) {
            throw new ValidationException("VBScript is not allowed in content");
        }
    }

    /**
     * Removes dangerous HTML content
     */
    private String removeDangerousContent(String content) {
        String result = content;
        
        // Remove script tags
        result = SCRIPT_PATTERN.matcher(result).replaceAll("");
        
        // Remove iframe tags
        result = IFRAME_PATTERN.matcher(result).replaceAll("");
        
        // Remove object tags
        result = OBJECT_PATTERN.matcher(result).replaceAll("");
        
        // Remove embed tags
        result = EMBED_PATTERN.matcher(result).replaceAll("");
        
        // Remove form tags
        result = FORM_PATTERN.matcher(result).replaceAll("");
        
        return result;
    }

    /**
     * Removes dangerous links
     */
    private String removeDangerousLinks(String content) {
        String result = content;
        
        // Remove javascript: links
        result = JAVASCRIPT_PATTERN.matcher(result).replaceAll("http:");
        
        // Remove vbscript: links
        result = VBSCRIPT_PATTERN.matcher(result).replaceAll("http:");
        
        // Remove data: links
        result = DATA_PATTERN.matcher(result).replaceAll("http:");
        
        return result;
    }

    /**
     * Removes dangerous attributes
     */
    private String removeDangerousAttributes(String content) {
        String result = content;
        
        // Remove event handlers
        result = ONLOAD_PATTERN.matcher(result).replaceAll("data-removed=");
        
        return result;
    }

    /**
     * Escapes HTML entities in markdown content
     */
    public String escapeHtmlEntities(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        return content
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }

    /**
     * Validates that content doesn't contain executable code patterns
     */
    public void validateNoExecutableCode(String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }

        // Check for common executable patterns
        String[] executablePatterns = {
            "<?php", "<?=", "<%", "<script", "eval(", "exec(", "system(",
            "Runtime.getRuntime()", "ProcessBuilder", "cmd.exe", "/bin/sh",
            "powershell", "bash", "sh -c"
        };

        String lowerContent = content.toLowerCase();
        for (String pattern : executablePatterns) {
            if (lowerContent.contains(pattern.toLowerCase())) {
                throw new ValidationException("Executable code patterns are not allowed in content");
            }
        }
    }
}
