package com.example.OnePieceTheoryEvaluator.controller;

import com.example.OnePieceTheoryEvaluator.dto.LoginRequest;
import com.example.OnePieceTheoryEvaluator.dto.RegisterRequest;
import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Response> registerUser(@RequestBody @Valid RegisterRequest registerRequest, @RequestParam(value = "imageFile", required=false) MultipartFile imageFile){
        return ResponseEntity.ok(userService.registerUser(registerRequest, imageFile));
    }

    @PostMapping("/login")
    public ResponseEntity<Response> loginUser(@RequestBody @Valid LoginRequest loginRequest){
        return ResponseEntity.ok(userService.loginUser(loginRequest));
    }
}