package com.example.Idempotency.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @Column(columnDefinition = "TEXT")
    private String requestBody;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    private int statusCode;

    private String deviceFingerprint;

    private LocalDateTime createdAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
