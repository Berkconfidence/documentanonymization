package com.example.documentanonymization.repository;

import com.example.documentanonymization.entity.Reviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewerRepository extends JpaRepository<Reviewer, Long> {
    Reviewer findByEmail(String email);
}
