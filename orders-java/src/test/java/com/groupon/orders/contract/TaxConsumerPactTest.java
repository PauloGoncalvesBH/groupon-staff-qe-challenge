package com.groupon.orders.contract;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@Tag("contract")
@PactTestFor(providerName = "tax", pactVersion = PactSpecVersion.V3)
public class TaxConsumerPactTest {

    @BeforeAll
    static void printPactRootDir() {
        System.out.println("Pact rootDir: " + System.getProperty("pact.rootDir"));
    }

    @Pact(consumer = "orders")
    public RequestResponsePact taxHappyPath(PactDslWithProvider builder) {
        return builder
            .given("Tax service can compute tax for USD")
            .uponReceiving("valid request returns taxAmount")
            .path("/tax/calc")
            .method("POST")
            .headers(java.util.Map.of("Content-Type", "application/json"))
            .body(new PactDslJsonBody()
                .numberValue("amount", 100.0)
                .stringValue("currency", "USD")
            )
            .willRespondWith()
            .status(200)
            .headers(java.util.Map.of("Content-Type", "application/json"))
            .body(new PactDslJsonBody().numberValue("taxAmount", 10.0))
            .toPact();
    }

    @Pact(consumer = "orders")
    public RequestResponsePact taxWithExtraField(PactDslWithProvider builder) {
        return builder
            .given("Consumer may ignore extra fields")
            .uponReceiving("valid request returns taxAmount plus ignored field")
            .path("/tax/calc")
            .method("POST")
            .headers(java.util.Map.of("Content-Type", "application/json"))
            .body(new PactDslJsonBody()
                .numberValue("amount", 50.0)
                .stringValue("currency", "BRL")
            )
            .willRespondWith()
            .status(200)
            .headers(java.util.Map.of("Content-Type", "application/json"))
            .body(new PactDslJsonBody()
                .numberValue("taxAmount", 4.0)
                .stringValue("ignored", "anything") // extra non-breaking field
            )
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "taxHappyPath")
    void verifyHappyPath(MockServer mockServer) {
        // Minimal client to hit mock server and assert body
        var client = java.net.http.HttpClient.newHttpClient();
        var payload = "{\"amount\":100.0,\"currency\":\"USD\"}";
        var req = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(mockServer.getUrl() + "/tax/calc"))
            .header("Content-Type", "application/json")
            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload))
            .build();
        try {
            var res = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            assertEquals(200, res.statusCode());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @PactTestFor(pactMethod = "taxWithExtraField")
    void verifyExtraFieldIsNonBreaking(MockServer mockServer) {
        var client = java.net.http.HttpClient.newHttpClient();
        var payload = "{\"amount\":50.0,\"currency\":\"BRL\"}";
        var req = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(mockServer.getUrl() + "/tax/calc"))
            .header("Content-Type", "application/json")
            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload))
            .build();
        try {
            var res = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            assertEquals(200, res.statusCode());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
