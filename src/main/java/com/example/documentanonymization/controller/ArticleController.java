package com.example.documentanonymization.controller;

import com.example.documentanonymization.entity.Article;
import com.example.documentanonymization.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    public ArticleController() {}

    @GetMapping
    public List<Article> getAllArticle() {
        return articleService.getAllArticle();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticle(@PathVariable Long id) {
        Optional<Article> article = articleService.getArticleById(id);
        return article.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<Article> getArticleByTrackingNumber(@PathVariable String trackingNumber) {
        Optional<Article> article = articleService.getArticleByTrackingNumber(trackingNumber);
        return article.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public List<Article> getArticleByAuthorEmail(@PathVariable String email) {
        return articleService.getArticleByAuthorEmail(email);
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
}
