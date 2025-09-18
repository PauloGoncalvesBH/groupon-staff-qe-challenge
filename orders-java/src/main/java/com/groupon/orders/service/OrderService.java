package com.groupon.orders.service;

import com.groupon.orders.api.OrderRequest;
import com.groupon.orders.kafka.PaymentPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {
    @Value("${TAX_SERVICE_URL}")
    private String taxServiceUrl;

    @Autowired
    private PaymentPublisher paymentPublisher;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> processOrder(OrderRequest request) {
        Double taxAmount = null;
        boolean fallback = false;
        try {
            Map<String, Object> taxPayload = new HashMap<>();
            taxPayload.put("amount", request.getAmount());
            taxPayload.put("currency", request.getCurrency());
            taxPayload.put("userId", request.getUserId());
            ResponseEntity<Map> response = restTemplate.postForEntity(taxServiceUrl, taxPayload, Map.class);
            taxAmount = (Double) response.getBody().get("taxAmount");
        } catch (Exception e) {
            // Fallback tax logic
            taxAmount = 10.0; // Flat rate fallback
            fallback = true;
        }
        // Publish event to Kafka only if TaxService succeeded
        if (!fallback) {
            paymentPublisher.publishPaymentStatusUpdated(request.getOrderId(), "success");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", request.getOrderId());
        result.put("taxAmount", taxAmount);
        result.put("status", fallback ? "fallback" : "success");
        return result;
    }
}
