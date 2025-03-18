package com.example.documentanonymization.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name="message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String senderEmail;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name="article_id", nullable=false)
    private Article article;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sentDate;
}
