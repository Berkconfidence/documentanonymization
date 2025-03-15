package com.example.documentanonymization.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name="article")
public class Article {

    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String authorEmail;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String trackingNumber;

    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionDate;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Reviewer assignedReviewer;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Reviewer getAssignedReviewer() {
        return assignedReviewer;
    }

    public void setAssignedReviewer(Reviewer assignedReviewer) {
        this.assignedReviewer = assignedReviewer;
    }
}
