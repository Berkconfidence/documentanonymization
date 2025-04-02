package com.example.documentanonymization.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] file;

    @Column(nullable = true, columnDefinition = "LONGBLOB")
    private byte[] anonymizedFile; // Anonimleştirilmiş belge

    @Column(nullable = true, columnDefinition = "LONGBLOB")
    private byte[] reviewedFile; // Hakem değerlendirmesi sonrası belge

    @Column(nullable = true, columnDefinition = "TEXT")
    private String reviewComment;

    @Column(nullable = false)
    private String authorEmail;

    @Column(nullable = false)
    private String status;   // "Alındı", "Değerlendirmede", "Değerlendirildi", "Yazara İletildi","Revize"

    @Column(nullable = false)
    private String trackingNumber;

    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reviewDate;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Reviewer assignedReviewer;

    @ElementCollection
    @CollectionTable(name = "article_specializations", joinColumns = @JoinColumn(name = "article_id"))
    private List<String> specializations = new ArrayList<>();

    public Long getId() {
        return id;
    }

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

    public byte[] getAnonymizedFile() {
        return anonymizedFile;
    }

    public void setAnonymizedFile(byte[] anonymizedFile) {
        this.anonymizedFile = anonymizedFile;
    }

    public byte[] getReviewedFile() {
        return reviewedFile;
    }

    public void setReviewedFile(byte[] reviewedFile) {
        this.reviewedFile = reviewedFile;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
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

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    public List<String> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(List<String> specializations) {
        this.specializations = specializations;
    }
}
