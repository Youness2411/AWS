package com.example.OnePieceTheoryEvaluator.service.impl;


import com.example.OnePieceTheoryEvaluator.dto.ChangePasswordRequest;
import com.example.OnePieceTheoryEvaluator.dto.LoginRequest;
import com.example.OnePieceTheoryEvaluator.dto.RegisterRequest;
import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.dto.UserDTO;
import com.example.OnePieceTheoryEvaluator.entity.User;
import com.example.OnePieceTheoryEvaluator.enums.UserRole;
import com.example.OnePieceTheoryEvaluator.exceptions.InvalidCredentialsException;
import com.example.OnePieceTheoryEvaluator.exceptions.NotFoundException;
import com.example.OnePieceTheoryEvaluator.exceptions.ValidationException;
import com.example.OnePieceTheoryEvaluator.repository.UserRepository;
import com.example.OnePieceTheoryEvaluator.security.JwtUtils;
import com.example.OnePieceTheoryEvaluator.service.UserService;
import com.example.OnePieceTheoryEvaluator.security.FileUploadSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;
    private final FileUploadSecurityService fileUploadSecurityService;
    
    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.images.dir}")
    private String imageDirectory;


    @Override
    public Response registerUser(RegisterRequest registerRequest, MultipartFile imageFile) {

        UserRole role = UserRole.USER;

        if (registerRequest.getRole() != null) {
            role=registerRequest.getRole();
        }

        User userToSave = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .build();

        //check if image is associated with the request
        if (imageFile != null && !imageFile.isEmpty()){
            // Validate file security
            fileUploadSecurityService.validateFile(imageFile);
            String filename = saveImage(imageFile);
            userToSave.setImageUrl(baseUrl + "/api/uploads/image/" + filename);
        }

        userRepository.save(userToSave);

        return Response.builder()
                .status(200)
                .message("user created successfully")
                .build();
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {
       User user = userRepository.findByEmail(loginRequest.getEmail())
               .orElseThrow(()-> new NotFoundException("Email not Found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("password does not match");
        }
        String token = jwtUtils.generateToken(user.getEmail());

        return Response.builder()
                .status(200)
                .message("user logged in successfully")
                .role(user.getRole())
                .token(token)
                .expirationTime("6 month")
                .build();
    }

    @Override
    public Response getAllUsers() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDTO> userDTOS = modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());

        userDTOS.forEach(userDTO -> userDTO.setTheories(null));
        userDTOS.forEach(userDTO -> userDTO.setComments(null));

        return Response.builder()
                .status(200)
                .message("success")
                .users(userDTOS)
                .build();
    }

    @Override
    public User getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("User Not Found"));

        // Create a new User instance to avoid modifying the managed entity
        User safeUser = User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(null) // Don't expose password
                .imageUrl(user.getImageUrl())
                .role(user.getRole())
                .theories(null) // Don't load relations
                .comments(null) // Don't load relations
                .build();

        return safeUser;
    }

    @Override
    public Response updateUser(Long id, UserDTO userDTO, MultipartFile imageFile) {

        User existingUser = userRepository.findById(id)
			.orElseThrow(()-> new NotFoundException("User Not Found"));
        //check if image is associated with the update request
        if (imageFile != null && !imageFile.isEmpty()){
            // Validate file security
            fileUploadSecurityService.validateFile(imageFile);
            String filename = saveImage(imageFile);
            existingUser.setImageUrl(baseUrl + "/api/uploads/image/" + filename);
        }
        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getUsername() != null) existingUser.setUsername(userDTO.getUsername());
        if (userDTO.getRole() != null) existingUser.setRole(userDTO.getRole());

        userRepository.save(existingUser);

        return Response.builder()
                .status(200)
                .message("User Successfully updated")
                .build();
    }

    @Override
    public Response deleteUser(Long id) {

         userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("User Not Found"));
         userRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("User Successfully Deleted")
                .build();
    }

    @Override
    public Response updateUserRole(Long id, String role) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));
        try{
            UserRole newRole = UserRole.valueOf(role.toUpperCase());
            existingUser.setRole(newRole);
            userRepository.save(existingUser);
        }catch(IllegalArgumentException e){
            throw new NotFoundException("Invalid role");
        }
        return Response.builder()
                .status(200)
                .message("Role updated")
                .build();
    }


	private String saveImage(MultipartFile imageFile){
        //create the directory to store images if it doesn't exist
        File directory = new File(imageDirectory);

        if (!directory.exists()){
            directory.mkdirs();
            log.info("Directory was created: {}", imageDirectory);
        }
        
        // Sanitize filename for security
        String sanitizedFilename = fileUploadSecurityService.sanitizeFilename(imageFile.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID() + "_" + sanitizedFilename;
        
        //get the absolute path of the image
        String imagePath = imageDirectory + (imageDirectory.endsWith("/") ? "" : "/") + uniqueFileName;

        try {
            File destinationFile = new File(imagePath);
            imageFile.transferTo(destinationFile); //we are transfering(writing to this folder)
            log.info("Image saved successfully: {}", uniqueFileName);

        }catch (Exception e){
            log.error("Error occurred while saving image: {}", e.getMessage());
            throw new IllegalArgumentException("Error occurred while saving image: " + e.getMessage());
        }

        return uniqueFileName;
    }

    @Override
    public Response changePassword(ChangePasswordRequest changePasswordRequest) {
        // Validate that new password and confirmation match
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            throw new ValidationException("New password and confirmation do not match");
        }

        // Get current user with password intact for verification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        // Verify current password
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), currentUser.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        // Check if new password is different from current password
        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), currentUser.getPassword())) {
            throw new ValidationException("New password must be different from current password");
        }

        // Update password
        currentUser.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(currentUser);

        log.info("Password changed successfully for user: {}", currentUser.getEmail());

        return Response.builder()
                .status(200)
                .message("Password changed successfully")
                .build();
    }

}