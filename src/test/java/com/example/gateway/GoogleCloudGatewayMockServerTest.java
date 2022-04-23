package com.example.gateway;

import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class GoogleCloudGatewayMockServerTest {

    static ClientAndServer mockServer;

    @BeforeAll
    public static void init() {
        mockServer = ClientAndServer.startClientAndServer(8888);
    }

    @AfterAll
    public static void stopServer() {
        mockServer.stop();
    }


    @Test
    void testUnknownRouter() {

        new MockServerClient("localhost", 8888)
                .when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/unknown_api/something"),
                        Times.unlimited())
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("myHeader", "myHeader_value"))
                                .withBody("Hello")
                                .withDelay(TimeUnit.SECONDS, 1)
                );


        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8080")
                .build().get().uri("/unknown/something")
                .exchange()
                .expectHeader().valueEquals("myHeader", "myHeader_value")
                .expectStatus().isOk()
                .expectBody().consumeWith(response -> Assertions.assertThat(new String(Optional.ofNullable(response.getResponseBody())
                                .orElse(new byte[]{})))
                        .isEqualTo("Hello"));

    }

}
