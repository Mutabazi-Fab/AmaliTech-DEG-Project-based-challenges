package com.example.Idempotency.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String message;
    private int status;
    private boolean isCacheHit;
}
