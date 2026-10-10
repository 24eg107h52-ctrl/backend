package com.example.feedback.controller;

import com.example.feedback.entity.Feedback;
import com.example.feedback.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;

    @Autowired
    public FeedbackController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    // =========================================================================
    // REST API ENDPOINTS (For Separate FRONTEND Folder via JavaScript fetch)
    // =========================================================================

    /**
     * REST API: Get all feedback records as JSON
     * Endpoint: GET /api/feedback
     */
    @GetMapping("/api/feedback")
    @ResponseBody
    public ResponseEntity<List<Feedback>> getAllFeedbackApi() {
        try {
            List<Feedback> feedbackList = feedbackRepository.findAllByOrderByIdDesc();
            return ResponseEntity.ok(feedbackList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    /**
     * REST API: Submit a new feedback record from the frontend JSON payload
     * Endpoint: POST /api/feedback
     */
    @PostMapping("/api/feedback")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitFeedbackApi(@RequestBody Feedback feedback) {
        Map<String, Object> response = new HashMap<>();

        // Validation
        if (feedback.getStudentName() == null || feedback.getStudentName().trim().isEmpty() ||
            feedback.getTeacherName() == null || feedback.getTeacherName().trim().isEmpty() ||
            feedback.getSubject() == null || feedback.getSubject().trim().isEmpty() ||
            feedback.getRating() == null || feedback.getRating() < 1 || feedback.getRating() > 5 ||
            feedback.getComments() == null || feedback.getComments().trim().isEmpty()) {

            response.put("success", false);
            response.put("message", "All fields are required. Rating must be between 1 and 5.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Feedback savedFeedback = feedbackRepository.save(feedback);
            response.put("success", true);
            response.put("message", "Feedback submitted successfully!");
            response.put("data", savedFeedback);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =========================================================================
    // THYMELEAF / MVC ENDPOINTS (For Server-Side Rendering)
    // =========================================================================

    @GetMapping("/")
    public String showFeedbackPage(Model model) {
        if (!model.containsAttribute("feedback")) {
            model.addAttribute("feedback", new Feedback());
        }

        try {
            model.addAttribute("feedbackList", feedbackRepository.findAllByOrderByIdDesc());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Could not retrieve feedback records from database: " + e.getMessage());
            model.addAttribute("feedbackList", Collections.emptyList());
        }

        return "index";
    }

    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute("feedback") Feedback feedback,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (feedback.getStudentName() == null || feedback.getStudentName().trim().isEmpty() ||
            feedback.getTeacherName() == null || feedback.getTeacherName().trim().isEmpty() ||
            feedback.getSubject() == null || feedback.getSubject().trim().isEmpty() ||
            feedback.getRating() == null || feedback.getRating() < 1 || feedback.getRating() > 5 ||
            feedback.getComments() == null || feedback.getComments().trim().isEmpty()) {

            model.addAttribute("errorMessage", "All fields are required. Please ensure rating is between 1 and 5.");
            model.addAttribute("feedback", feedback);

            try {
                model.addAttribute("feedbackList", feedbackRepository.findAllByOrderByIdDesc());
            } catch (Exception e) {
                model.addAttribute("feedbackList", Collections.emptyList());
            }

            return "index";
        }

        try {
            feedbackRepository.save(feedback);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback submitted successfully!");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to save feedback to MySQL: " + e.getMessage());
            model.addAttribute("feedback", feedback);

            try {
                model.addAttribute("feedbackList", feedbackRepository.findAllByOrderByIdDesc());
            } catch (Exception ex) {
                model.addAttribute("feedbackList", Collections.emptyList());
            }

            return "index";
        }
    }
}
