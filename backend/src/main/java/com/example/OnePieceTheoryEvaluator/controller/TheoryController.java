package com.example.OnePieceTheoryEvaluator.controller;

import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.dto.TheoryDTO;
import com.example.OnePieceTheoryEvaluator.service.TheoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/theories")
@RequiredArgsConstructor
public class TheoryController {

    private final TheoryService theoryService;


    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> postTheory(
            @ModelAttribute TheoryDTO theoryDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ){
        try {
            return ResponseEntity.ok(theoryService.postTheory(theoryDTO, image));
        } catch (Exception ex) {
            // This will be caught by GlobalExceptionHandler and return a generic error
            throw ex;
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> updateTheory(
            @PathVariable Long id,
            @ModelAttribute TheoryDTO theoryDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ){
        return ResponseEntity.ok(theoryService.updateTheory(id, theoryDTO, image));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response> deleteTheory(@PathVariable Long id){
        return ResponseEntity.ok(theoryService.deleteTheory(id));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllTheories(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ){
        if (page == 0 && size == 10) {
            // Use non-paginated version for backward compatibility
            return ResponseEntity.ok(theoryService.getAllTheories());
        } else {
            // Use paginated version
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size);
            return ResponseEntity.ok(theoryService.getAllTheories(pageable));
        }
    }

    @GetMapping("/flagged")
    public ResponseEntity<Response> getAllTheoriesFlagged(){
        return ResponseEntity.ok(theoryService.getAllTheoriesFlagged());
    }

    @GetMapping("/query")
    public ResponseEntity<Response> queryTheories(
            @RequestParam(value = "q", required = false) String searchText,
            @RequestParam(value = "sort", required = false, defaultValue = "date") String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ){
        if (page == 0 && size == 10) {
            // Use non-paginated version for backward compatibility
            return ResponseEntity.ok(theoryService.queryTheories(searchText, sortBy));
        } else {
            // Use paginated version
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size);
            return ResponseEntity.ok(theoryService.queryTheories(searchText, sortBy, pageable));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getTheoryById(@PathVariable Long id){
        return ResponseEntity.ok(theoryService.getTheoryById(id));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Response> getTheoryByUserId(@PathVariable Long id){
        return ResponseEntity.ok(theoryService.getTheoryByUserId(id));
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<Response> getTheoryVersions(@PathVariable Long id){
        return ResponseEntity.ok(theoryService.getTheoryVersions(id));
    }

    @GetMapping("/trending")
    public ResponseEntity<Response> getTrendingTheories(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ){
        return ResponseEntity.ok(theoryService.getTrendingTheories(limit));
    }

    @GetMapping("/last-chapter")
    public ResponseEntity<Response> getLastChapterTheories(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ){
        return ResponseEntity.ok(theoryService.getLastChapterTheories(limit));
    }
    
}


