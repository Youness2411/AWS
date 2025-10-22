package com.example.OnePieceTheoryEvaluator.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.dto.TheoryDTO;

public interface TheoryService {
    Response postTheory(TheoryDTO theoryDTO, MultipartFile imageFile);
    Response updateTheory(Long id, TheoryDTO theoryDTO, MultipartFile imageFile);
    Response deleteTheory(Long id);
    Response getAllTheories();
    Response getAllTheories(Pageable pageable);
    Response getAllTheoriesFlagged();
    Response getTheoryById(Long id);
    Response getTheoryByUserId(Long id); //User id

    Response queryTheories(String searchText, String sortBy);
    Response queryTheories(String searchText, String sortBy, Pageable pageable);

    Response getTheoryVersions(Long id);

    Response getTrendingTheories(int limit);

    Response getLastChapterTheories(int limit);
}
