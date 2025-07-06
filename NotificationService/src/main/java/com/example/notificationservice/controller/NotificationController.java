package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        System.out.println("📧 SAHTE MAIL LOG:");
        System.out.println("  To: " + request.getEmail());
        System.out.println("  Message: " + request.getMessage());

        return ResponseEntity.ok("Notification pretend-sent to " + request.getEmail());
    }
}
