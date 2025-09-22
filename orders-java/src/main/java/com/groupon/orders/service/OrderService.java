package com.groupon.orders.service;

import com.groupon.orders.api.OrderRequest;
import com.groupon.orders.kafka.PaymentPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final RestTemplate restTemplate;
    private final PaymentPublisher paymentPublisher;
    private final String taxServiceUrl;
    private final String appEnv;

    public OrderService(
            RestTemplate restTemplate,
            PaymentPublisher paymentPublisher,
            @Value("${TAX_SERVICE_URL}") String taxServiceUrl,
            @Value("${APP_ENV:development}") String appEnv) {
        this.restTemplate = restTemplate;
        this.paymentPublisher = paymentPublisher;
        this.taxServiceUrl = taxServiceUrl;
        this.appEnv = appEnv;
    }

    public Map<String, Object> processOrder(OrderRequest request) {
        Double taxAmount = null;
        boolean fallback = false;
        try {
            Map<String, Object> taxPayload = new HashMap<>();
            taxPayload.put("amount", request.getAmount());
            taxPayload.put("currency", request.getCurrency());
            ResponseEntity<Map> response = restTemplate.postForEntity(taxServiceUrl + "/tax/calc", taxPayload, Map.class);
            taxAmount = (Double) response.getBody().get("taxAmount");
        } catch (Exception e) {
            if ("production".equalsIgnoreCase(appEnv)) {
                log.warn("TaxService call failed: {}", e.getMessage());
            } else {
                log.error("TaxService call failed", e);
            }
            // Fallback tax logic
            taxAmount = 10.0; // Flat rate fallback
            fallback = true;
        }
        // Publish event to Kafka only if TaxService succeeded
        if (!fallback) {
            paymentPublisher.publishPaymentStatusUpdated(
                request.getOrderId(),
                "success",
                request.getAmount(),
                taxAmount
            );
        }
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", request.getOrderId());
        result.put("taxAmount", taxAmount);
        result.put("status", fallback ? "fallback" : "success");
        return result;
    }
}
