package com.example.OnePieceTheoryEvaluator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.images.dir:${user.dir}/product-image/}")
    private String imagesDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files from the filesystem directory at /images/**
        String location = "file:" + (imagesDir.endsWith("/") ? imagesDir : imagesDir + "/");
        System.out.println("Configuring image serving from: " + location);
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location)
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}


