package com.example.OnePieceTheoryEvaluator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.util.ErrorResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Configuration for error handling
 */
@Configuration
public class ErrorHandlingConfig implements WebMvcConfigurer {

    /**
     * Custom error controller to handle unmapped errors
     */
    @RestController
    public static class CustomErrorController implements ErrorController {

        @RequestMapping("/error")
        public ResponseEntity<Response> handleError() {
            return ErrorResponseUtil.createGenericErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
