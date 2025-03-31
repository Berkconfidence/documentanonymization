package com.example.documentanonymization.controller;

import com.example.documentanonymization.entity.Reviewer;
import com.example.documentanonymization.service.ReviewerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/reviewers")
public class ReviewerController {

    @Autowired
    private ReviewerService reviewerService;

    public ReviewerController() {}

    @GetMapping("/all")
    public ResponseEntity<?> getAllReviewers() {
        return reviewerService.getAllReviewers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewer(@PathVariable long id) {
        return reviewerService.getReviewerById(id);
    }

    @GetMapping("/trackingnumber/{trackingNumber}")
    public ResponseEntity<?> getReviewerByArticleNumber(@PathVariable String trackingNumber) {
        return reviewerService.getReviewerByArticleNumber(trackingNumber);
    }

    @PostMapping("/create")
    public ResponseEntity<Reviewer> createReviewer(MultipartHttpServletRequest request) {
        try {
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String specializations = request.getParameter("specializations");

            List<String> parsespecializations = reviewerService.parseSpecializations(specializations);
            Reviewer reviewer = reviewerService.createReviewer(name, email, parsespecializations);
            return ResponseEntity.ok().body(reviewer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Reviewer> updateReviewer(MultipartHttpServletRequest request) {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String specializations = request.getParameter("specializations");

            List<String> parsespecializations = reviewerService.parseSpecializations(specializations);
            Reviewer reviewer = reviewerService.updateReviewer(id, name, email, parsespecializations);
            return ResponseEntity.ok().body(reviewer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteReviewer(@RequestParam Long id) {
        return reviewerService.deleteReviewer(id);
    }

}
