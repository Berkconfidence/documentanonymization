package com.example.documentanonymization.controller;

import com.example.documentanonymization.entity.Article;
import com.example.documentanonymization.entity.Reviewer;
import com.example.documentanonymization.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    public ArticleController() {}

    @GetMapping
    public ResponseEntity<?> getAllArticle() {
        return articleService.getAllArticle();
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<?> getArticleByTrackingNumber(@PathVariable String trackingNumber) {
        return articleService.getArticleByTrackingNumber(trackingNumber);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getArticlesByAuthorEmail(@PathVariable String email) {
        return articleService.getArticleByAuthorEmail(email);
    }

    @GetMapping("/download/{trackingNumber}")
    public ResponseEntity<byte[]> downloadArticle(@PathVariable String trackingNumber) {
        return articleService.downloadArticleFile(trackingNumber);
    }

    @GetMapping("/view/{trackingNumber}")
    public ResponseEntity<byte[]> viewAnonimizeArticleFile(@PathVariable String trackingNumber) {
        return articleService.viewAnonimizeArticleFile(trackingNumber);
    }

    @GetMapping("/reviewer/{id}")
    public ResponseEntity<?> getArticlesByReviewerId(@PathVariable Long id) {
        return articleService.getArticlesByReviewerId(id);
    }

    @PostMapping("/create")
    public ResponseEntity<Article> createArticle(@RequestParam("file") MultipartFile file, @RequestParam("email") String email) {
        try {
            Article newArticle = articleService.createArticle(file, email);
            return ResponseEntity.ok(newArticle);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/anonimize/{trackingNumber}")
    public ResponseEntity<?> anonimizeArticle(@PathVariable String trackingNumber) {
        try {
            System.out.println("Anonimleştirme isteği alındı: " + trackingNumber);
            Article updatedArticle = articleService.anonimizeArticle(trackingNumber);
            System.out.println("Anonimleştirme başarılı");
            return ResponseEntity.ok(updatedArticle);
        } catch (IOException e) {
            System.err.println("I/O Hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("I/O Hatası: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Beklenmeyen hata: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Beklenmeyen hata: " + e.getMessage());
        }
    }

    @PostMapping("/assignReviewer/{trackingNumber}")
    public ResponseEntity<?> assignReviewer(@PathVariable String trackingNumber, @RequestBody Reviewer reviewer) {
        try {
            Article updatedArticle = articleService.assignReviewer(trackingNumber, reviewer);
            return ResponseEntity.ok(updatedArticle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Beklenmeyen hata: " + e.getMessage());
        }
    }

    @PostMapping("/review/{trackingNumber}")
    public ResponseEntity<?> reviewArticle(MultipartHttpServletRequest request) {
        try {
            String reviewText = request.getParameter("reviewText");
            String trackingNumber = request.getParameter("trackingNumber");
            Article updatedArticle = articleService.reviewArticle(reviewText, trackingNumber);
            return ResponseEntity.ok(updatedArticle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Beklenmeyen hata: " + e.getMessage());
        }
    }
}
