package com.groupon.orders.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentPublisher {
    @Value("${KAFKA_BOOTSTRAP_SERVERS:redpanda:9092}")
    private String kafkaBootstrapServers;

    @Value("${PAYMENT_PROVIDER:stubbed}")
    private String paymentProvider;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentStatusUpdated(String orderId, String status, Double amount, Double taxAmount) {
        Map<String, Object> event = new HashMap<>();
        event.put("orderId", orderId);
        event.put("status", status);
        event.put("provider", paymentProvider);
        event.put("amount", amount);
        event.put("taxAmount", taxAmount);
        event.put("occurredAt", Instant.now().toString());
        kafkaTemplate.send("payments.status.updated", orderId, event);
    }
}
