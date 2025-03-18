package com.example.documentanonymization.service;

import com.example.documentanonymization.entity.Article;
import com.example.documentanonymization.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    public ArticleService() {}


    public List<Article> getAllArticle() {
        return articleRepository.findAll();
    }

    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }

    public Optional<Article> getArticleByTrackingNumber(String trackingNumber) {
        return articleRepository.findByTrackingNumber(trackingNumber);
    }

    public List<Article> getArticleByAuthorEmail(String email) {
        List<Article> articles = articleRepository.findAll();
        return articles.stream()
                .filter(article -> article.getAuthorEmail().equals(email)).
                map(article -> {
                    Article dto = new Article();
                    dto.setFile(article.getFile());
                    dto.setAuthorEmail(article.getAuthorEmail());
                    dto.setStatus(article.getStatus());
                    dto.setTrackingNumber(article.getTrackingNumber());
                    dto.setSubmissionDate(article.getSubmissionDate());
                    dto.setReviewDate(article.getReviewDate());
                    return dto;
                }).collect(Collectors.toList());
    }

    public Article createArticle(MultipartFile file, String email) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Dosya boş olamaz");
        }

        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Sadece PDF dosyaları kabul edilir");
        }

        String trackingNumber = calculateRandomNumber();
        while(true) {
            if(articleRepository.findByTrackingNumber(trackingNumber).isEmpty()) {
                break;
            }
            else
                trackingNumber = calculateRandomNumber();
        }

        Article article = new Article();
        article.setFile(file.getBytes());
        article.setAuthorEmail(email);
        article.setStatus("Kabul");
        article.setTrackingNumber(trackingNumber);
        article.setSubmissionDate(new Date());
        return articleRepository.save(article);
    }

    public String calculateRandomNumber() {
        Random random = new Random();
        int randomNumber = 10000000 + random.nextInt(90000000);
        return String.valueOf(randomNumber);
    }
}
