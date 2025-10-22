package com.example.OnePieceTheoryEvaluator.service;

import org.springframework.web.multipart.MultipartFile;

import com.example.OnePieceTheoryEvaluator.dto.ChangePasswordRequest;
import com.example.OnePieceTheoryEvaluator.dto.LoginRequest;
import com.example.OnePieceTheoryEvaluator.dto.RegisterRequest;
import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.dto.UserDTO;
import com.example.OnePieceTheoryEvaluator.entity.User;

public interface UserService {
    Response registerUser(RegisterRequest registerRequest, MultipartFile imageFile);
    Response loginUser(LoginRequest loginRequest);
    Response getAllUsers();
    User getCurrentLoggedInUser();
    Response updateUser(Long id, UserDTO userDTO, MultipartFile imageFile);
    Response deleteUser(Long id);
    Response updateUserRole(Long id, String role);
    Response changePassword(ChangePasswordRequest changePasswordRequest);
}
