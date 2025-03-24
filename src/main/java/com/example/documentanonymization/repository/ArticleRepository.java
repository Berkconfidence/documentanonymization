package com.example.documentanonymization.repository;

import com.example.documentanonymization.entity.Article;
import com.example.documentanonymization.entity.Reviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findByTrackingNumber(String trackingNumber);
    List<Article> findByAuthorEmail(String trackingNumber);
    List<Article> findByAssignedReviewer(Reviewer reviewer);
}
