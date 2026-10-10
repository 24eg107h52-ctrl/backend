package com.example.feedback.repository;

import com.example.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Retrieve all feedback ordered by ID descending (newest submissions first)
    List<Feedback> findAllByOrderByIdDesc();
}
