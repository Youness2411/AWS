package com.example.OnePieceTheoryEvaluator.controller;

import com.example.OnePieceTheoryEvaluator.dto.ChangePasswordRequest;
import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.dto.UserDTO;
import com.example.OnePieceTheoryEvaluator.entity.User;
import com.example.OnePieceTheoryEvaluator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN','MODERATOR')")
    public ResponseEntity<Response> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> updateUser(
            @PathVariable Long id,
            @ModelAttribute UserDTO userDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ){
        return ResponseEntity.ok(userService.updateUser(id, userDTO, image));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteUser(@PathVariable Long id){
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @PutMapping("/role/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateUserRole(@PathVariable Long id, @RequestParam("role") String role){
        return ResponseEntity.ok(userService.updateUserRole(id, role));
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(){
        return ResponseEntity.ok(userService.getCurrentLoggedInUser());
    }

    @PutMapping("/change-password")
    public ResponseEntity<Response> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest){
        return ResponseEntity.ok(userService.changePassword(changePasswordRequest));
    }
}