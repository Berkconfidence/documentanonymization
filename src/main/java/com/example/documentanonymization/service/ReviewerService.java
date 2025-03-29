package com.example.documentanonymization.service;

import com.example.documentanonymization.dto.ArticleDto;
import com.example.documentanonymization.dto.ReviewerDto;
import com.example.documentanonymization.entity.Article;
import com.example.documentanonymization.entity.Reviewer;
import com.example.documentanonymization.repository.ArticleRepository;
import com.example.documentanonymization.repository.ReviewerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewerService {

    @Autowired
    private ReviewerRepository reviewerRepository;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ObjectMapper objectMapper;

    public ReviewerService() {}

    public ResponseEntity<List<ReviewerDto>> getAllReviewers() {
        List<Reviewer> reviewers = reviewerRepository.findAll();

        if (reviewers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<ReviewerDto> dtoList = reviewers.stream()
                .map(reviewer -> {
                    int activeArticleCount = articleService.getReviewersActiveArticlesCount(reviewer);
                    int completedArticleCount = articleService.getReviewersCompletedArticlesCount(reviewer);
                    ReviewerDto dto = new ReviewerDto();
                    dto.setId(reviewer.getId());
                    dto.setName(reviewer.getName());
                    dto.setEmail(reviewer.getEmail());
                    dto.setSpecializations(reviewer.getSpecializations());
                    dto.setActiveReviewsCount(activeArticleCount);
                    dto.setCompletedReviewsCount(completedArticleCount);
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    public ResponseEntity<ReviewerDto> getReviewerById(long id) {
        return reviewerRepository.findById(id)
                .map(reviewer -> {
                    int activeArticleCount = articleService.getReviewersActiveArticlesCount(reviewer);
                    int completedArticleCount = articleService.getReviewersCompletedArticlesCount(reviewer);
                    ReviewerDto dto = new ReviewerDto();
                    dto.setId(reviewer.getId());
                    dto.setName(reviewer.getName());
                    dto.setEmail(reviewer.getEmail());
                    dto.setSpecializations(reviewer.getSpecializations());
                    dto.setActiveReviewsCount(activeArticleCount);
                    dto.setCompletedReviewsCount(completedArticleCount);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Reviewer> getReviewerByArticleNumber(String trackingNumber) {
        ResponseEntity<ArticleDto> article = articleService.getArticleByTrackingNumber(trackingNumber);

        if (article.getStatusCode().isError()) {
            return ResponseEntity.status(article.getStatusCode()).body(null);
        }
        ArticleDto articleDto = article.getBody();
        if (articleDto == null) {
            return ResponseEntity.notFound().build();
        }
        Reviewer reviewer = articleDto.getAssignedReviewer();

        return ResponseEntity.ok(reviewer);
    }

    public List<String> parseSpecializations(String specializations) {
        try {
            return objectMapper.readValue(specializations, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Uzmanlık alanları ayrıştırılamadı", e);
        }
    }

    public Reviewer createReviewer(String name, String email, List<String> parsespecializations) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ad Soyad boş olamaz");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("E-posta boş olamaz");
        }

        if (parsespecializations == null || parsespecializations.isEmpty()) {
            throw new IllegalArgumentException("Uzmanlık alanları boş olamaz");
        }

        if (reviewerRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Bu e-posta adresi ile kayıtlı bir hakem zaten var");
        }

        Reviewer reviewer = new Reviewer();
        reviewer.setName(name);
        reviewer.setEmail(email);
        reviewer.setSpecializations(parsespecializations);
        return reviewerRepository.save(reviewer);
    }

    public Reviewer updateReviewer(Long id, String name, String email, List<String> parsespecializations) {
        if(id == null) {
            throw new IllegalArgumentException("Hakem id'si boş olamaz");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ad Soyad boş olamaz");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("E-posta boş olamaz");
        }

        if (parsespecializations == null || parsespecializations.isEmpty()) {
            throw new IllegalArgumentException("Uzmanlık alanları boş olamaz");
        }

        Optional<Reviewer> reviewerOpt = reviewerRepository.findById(id);
        Reviewer reviewer = reviewerOpt.get();
        reviewer.setName(name);
        reviewer.setEmail(email);
        reviewer.setSpecializations(parsespecializations);
        return reviewerRepository.save(reviewer);
    }

    public ResponseEntity<?> deleteReviewer(Long id) {
        Reviewer reviewer = reviewerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hakem bulunamadı"));

        articleService.deleteReviewerFromArticle(reviewer);
        reviewerRepository.delete(reviewer);
        return ResponseEntity.ok().build();
    }
}
