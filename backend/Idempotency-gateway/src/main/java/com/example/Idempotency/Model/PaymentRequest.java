package com.example.Idempotency.Model;

import lombok.Data;

@Data
public class PaymentRequest {
    private Double amount;
    private String currency;
}
