package com.groupon.orders.service;

import com.groupon.orders.api.OrderRequest;
import com.groupon.orders.kafka.PaymentPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
class OrderServiceTest {

    private RestTemplate restTemplate;
    private PaymentPublisher publisher;
    private OrderService service;

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        publisher = mock(PaymentPublisher.class);
        service = new OrderService(restTemplate, publisher, "http://tax:4567", "non-production");
    }

    private OrderRequest buildRequest() {
        OrderRequest r = new OrderRequest();
        r.setOrderId("o-1");
        r.setItems(List.of("a", "b"));
        r.setAmount(100.0);
        r.setCurrency("USD");
        r.setUserId("u");
        return r;
    }
    @Test 
    @DisplayName("Given valid order and Tax OK, when processing, then publishes and returns success")
    void givenValidOrderAndTaxOk_whenProcessingOrder_thenPublishesAndReturnsSuccess() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(ResponseEntity.ok(Map.of("taxAmount", 8.0)));

        // When
        var result = service.processOrder(buildRequest());

        // Then
        assertEquals("o-1", result.get("orderId"));
        assertEquals("success", result.get("status"));
        assertEquals(8.0, (Double) result.get("taxAmount"));

        // And publishes once with correct args
        verify(publisher, times(1))
            .publishPaymentStatusUpdated(eq("o-1"), eq("success"));
    }

    @Test
    @DisplayName("Given Tax service fails, when processing, then uses fallback and does not publish")
    void givenTaxServiceFails_whenProcessingOrder_thenUsesFallbackAndDoesNotPublish() {
        // Given: simulate error calling tax
        when(restTemplate.postForEntity(anyString(), any(Map.class), eq(Map.class)))
            .thenThrow(new RuntimeException("random failure"));

        // When
        var result = service.processOrder(buildRequest());

        // Then fallback is applied
        assertEquals("o-1", result.get("orderId"));
        assertEquals("fallback", result.get("status"));
        assertEquals(10.0, (Double) result.get("taxAmount"));
        // And no publish on kafka
        verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Given Tax service returns non-Double, when processing, then uses fallback and does not publish")
    void givenTaxServiceReturnsNonDouble_whenProcessingOrder_thenUsesFallbackAndDoesNotPublish() {
        // Given: tax return wrong type (Integer instead of Double)
        when(restTemplate.postForEntity(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(ResponseEntity.ok(Map.of("taxAmount", Integer.valueOf(8))));

        // When
        var result = service.processOrder(buildRequest());

        // Then fallback is applied
        assertEquals("fallback", result.get("status"));
        assertEquals(10.0, (Double) result.get("taxAmount"));
        // And no publish on kafka
        verifyNoInteractions(publisher);
    }
}
