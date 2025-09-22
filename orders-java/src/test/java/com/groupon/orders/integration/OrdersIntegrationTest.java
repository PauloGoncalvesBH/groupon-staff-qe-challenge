package com.groupon.orders.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class OrdersIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    static WireMockServer taxStub;

    static {
        taxStub = new WireMockServer(0);
        taxStub.start();
        WireMock.configureFor("localhost", taxStub.port());
    }

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("KAFKA_BOOTSTRAP_SERVERS", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("TAX_SERVICE_URL", () -> "http://localhost:" + taxStub.port());
    }

    @BeforeEach
    void resetAndDefaultStub() {
        taxStub.resetAll();
    }

    @AfterAll
    static void tearDownAll() {
        taxStub.stop();
    }

    @Test
    void shouldPublishPaymentTopicOnKafkaWhenTaxReturnsSuccess() throws Exception {
        // Given: Tax returns success with taxAmount=7.5
        taxStub.stubFor(post(urlEqualTo("/tax/calc"))
                .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"taxAmount\": 7.5}")));
        // Given: a Kafka consumer subscribed to the topic
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "orders-int-test");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("payments.status.updated"));

            // When: we POST an order to the running app
            String body = "{" +
                "\"orderId\":\"int-123\"," +
                "\"items\":[\"i1\"]," +
                "\"amount\":100.0," +
                "\"currency\":\"USD\"," +
                "\"userId\":\"u1\"" +
            "}";
            RestTemplate http = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            var response = http.postForEntity("http://localhost:" + port + "/orders", entity, String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful());

            // Then: we should receive one record on the topic
            ConsumerRecords<String, String> records = ConsumerRecords.empty();
            long deadline = System.currentTimeMillis() + 10000; // 10s timeout
            while (System.currentTimeMillis() < deadline && records.isEmpty()) {
                records = consumer.poll(Duration.ofMillis(500));
            }
            assertFalse(records.isEmpty(), "Expected a message on payments.status.updated");
            ConsumerRecord<String, String> record = records.iterator().next();
            assertEquals("int-123", record.key());
            String value = record.value();
            assertNotNull(value);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> event = mapper.readValue(value, Map.class);
            assertEquals("int-123", event.get("orderId"));
            assertEquals("success", event.get("status"));
            assertEquals(7.5, ((Number) event.get("taxAmount")).doubleValue(), 0.0001);
        }
    }

    @Test
    void shouldNotPublishPaymentTopicOnKafkaWhenTaxFails() throws Exception {
        // Given: Tax service returns 500
        taxStub.stubFor(post(urlEqualTo("/tax/calc"))
                .willReturn(aResponse().withStatus(500)));

        // And a Kafka consumer subscribed to the topic with a fresh group
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "orders-int-test-fail");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        String orderId = "int-err-1";
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("payments.status.updated"));

            // When: we POST an order to the running app
            String body = "{" +
                "\"orderId\":\"" + orderId + "\"," +
                "\"items\":[\"i1\"]," +
                "\"amount\":120.0," +
                "\"currency\":\"USD\"," +
                "\"userId\":\"u2\"" +
            "}";
            RestTemplate http = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            var response = http.postForEntity("http://localhost:" + port + "/orders", entity, String.class);
            assertTrue(response.getStatusCode().is2xxSuccessful());

            // And: response indicates fallback with taxAmount = 10.0
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = mapper.readValue(Objects.requireNonNull(response.getBody()), Map.class);
            assertEquals("fallback", result.get("status"));
            assertEquals(10.0, ((Number) result.get("taxAmount")).doubleValue(), 0.0001);

            // Then: no new event for this orderId should be published
            boolean found = false;
            long deadline = System.currentTimeMillis() + 10000; // 10s timeout
            while (System.currentTimeMillis() < deadline && !found) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> rec : records) {
                    if (orderId.equals(rec.key())) {
                        found = true;
                        break;
                    }
                }
            }
            assertFalse(found, "Did not expect a message for orderId=" + orderId + " when Tax failed");
        }
    }
}
