package com.example.documentanonymization.dto;

import java.util.ArrayList;
import java.util.List;

public class ReviewerDto {

    private Long id;
    private String name;
    private String email;
    private List<String> specializations = new ArrayList<>();
    private int activeReviewsCount;
    private int completedReviewsCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(List<String> specializations) {
        this.specializations = specializations;
    }

    public int getActiveReviewsCount() {
        return activeReviewsCount;
    }

    public void setActiveReviewsCount(int activeReviewsCount) {
        this.activeReviewsCount = activeReviewsCount;
    }

    public int getCompletedReviewsCount() {
        return completedReviewsCount;
    }

    public void setCompletedReviewsCount(int completedReviewsCount) {
        this.completedReviewsCount = completedReviewsCount;
    }
}
