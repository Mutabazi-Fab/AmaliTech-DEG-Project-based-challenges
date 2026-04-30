package com.example.Idempotency.Controller;

import com.example.Idempotency.Model.PaymentRequest;
import com.example.Idempotency.Model.PaymentResponse;
import com.example.Idempotency.Service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/process-payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<String> processPayment(
            @RequestHeader("Idempotency-Key") String key,
            @RequestHeader("User-Agent") String userAgent,
            @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.handlePayment(key, request, userAgent);

        var responseBuilder = ResponseEntity.status(response.getStatus());

        if (response.isCacheHit()) {
            responseBuilder.header("X-Cache-Hit", "true");
        }

        return responseBuilder.body(response.getMessage());
    }
}
