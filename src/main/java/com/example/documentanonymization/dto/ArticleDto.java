package com.example.documentanonymization.dto;

import com.example.documentanonymization.entity.Reviewer;

import java.util.Date;

public class ArticleDto {

    private String fileName;
    private byte[] file;
    private String authorEmail;
    private String status;
    private String trackingNumber;
    private Date submissionDate;
    private Date reviewDate;
    private Reviewer assignedReviewer;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
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

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    public Reviewer getAssignedReviewer() {
        return assignedReviewer;
    }

    public void setAssignedReviewer(Reviewer assignedReviewer) {
        this.assignedReviewer = assignedReviewer;
    }
}
