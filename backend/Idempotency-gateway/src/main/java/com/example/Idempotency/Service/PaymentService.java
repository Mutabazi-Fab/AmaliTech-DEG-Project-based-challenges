package com.example.Idempotency.Service;

import com.example.Idempotency.Model.Payment;
import com.example.Idempotency.Model.PaymentRequest;
import com.example.Idempotency.Model.PaymentResponse;
import com.example.Idempotency.Repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final ConcurrentHashMap<String, Object> activeLocks = new ConcurrentHashMap<>();

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public PaymentResponse handlePayment(String key, PaymentRequest request, String userAgent) {
        Object lock = activeLocks.computeIfAbsent(key, k -> new Object());

        synchronized (lock) {
            try {
                Optional<Payment> existing = repository.findByIdempotencyKey(key);

                if (existing.isPresent()) {
                    Payment payment = existing.get();

                    if (!payment.getDeviceFingerprint().equals(userAgent)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Security Alert: Idempotency key reuse detected from a different device.");
                    }

                    if (!payment.getRequestBody().equals(request.toString())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Key already used for a different request body.");
                    }
                    return new PaymentResponse(payment.getResponseBody(), payment.getStatusCode(), true);
                }

                simulateDelay(2000);

                String responseMsg = "Charged " + request.getAmount() + " " + request.getCurrency();

                Payment paymentToSave = new Payment();
                paymentToSave.setIdempotencyKey(key);
                paymentToSave.setRequestBody(request.toString());
                paymentToSave.setResponseBody(responseMsg);
                paymentToSave.setStatusCode(HttpStatus.OK.value());
                paymentToSave.setDeviceFingerprint(userAgent);

                repository.save(paymentToSave);

                return new PaymentResponse(responseMsg, HttpStatus.OK.value(), false);

            } finally {
                activeLocks.remove(key);
            }
        }
    }

    private void simulateDelay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
