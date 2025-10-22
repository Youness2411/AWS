package com.example.OnePieceTheoryEvaluator.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class SecurityConfig {

    /**
     * Configures multipart file upload settings with security constraints
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Maximum file size (5MB)
        factory.setMaxFileSize(DataSize.ofMegabytes(5));
        
        // Maximum request size (10MB total)
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));
        
        // File size threshold (1MB) - files larger than this will be written to disk
        factory.setFileSizeThreshold(DataSize.ofMegabytes(1));
        
        // Location for temporary files
        factory.setLocation(System.getProperty("java.io.tmpdir"));
        
        return factory.createMultipartConfig();
    }

    /**
     * Configures multipart resolver with security settings
     */
    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        
        // Enable multipart resolution
        resolver.setResolveLazily(true);
        
        return resolver;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
