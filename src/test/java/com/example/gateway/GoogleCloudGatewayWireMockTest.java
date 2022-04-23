package com.example.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class GoogleCloudGatewayWireMockTest {

    static WireMockServer mockServer;

    @BeforeAll
    static void init() {
        mockServer = new WireMockServer(
                new WireMockConfiguration().port(8888)
        );
        mockServer.start();
        WireMock.configureFor("localhost", 8888);
    }

    @AfterAll
    public static void stopServer() {
        mockServer.stop();
    }


    @Test
    void testUnknownRouter() {

        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/unknown_api/something"))
                .willReturn(WireMock.aResponse().withBody("Hello")
                        .withHeader("myHeader", "myHeader_value")
                        .withStatus(200)));

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
