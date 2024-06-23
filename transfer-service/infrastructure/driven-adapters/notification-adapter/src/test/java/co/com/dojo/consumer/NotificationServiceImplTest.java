package co.com.dojo.consumer;

import co.com.dojo.model.Notification;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificationServiceImplTest {

    private static NotificationServiceImpl notificationServiceImpl;

    private static MockWebServer mockBackEnd;

    private Notification notification;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        var webClient1 = WebClient.builder().baseUrl(mockBackEnd.url("/alfa").toString()).build();
        var webClient2 = WebClient.builder().baseUrl(mockBackEnd.url("/beta").toString()).build();

        notificationServiceImpl = new NotificationServiceImpl(webClient1, webClient2,
                Mappers.getMapper(NotificationMapper.class));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void before() {
        notification = Notification.builder()
                .phoneNumber("2341241234")
                .message("Test message")
                .build();
    }

    @SneakyThrows
    @Test
    @DisplayName("Performs a successful notification.")
    void shouldPerformSuccessfulNotification() {

        mockBackEnd.enqueue(new MockResponse()
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.OK.value())
            .setBody("""
                        {
                        "mobile": "2341241234",
                        "text": "Test message",
                        "timestamp": "Fri Jun 21 2024 22:37:17 GMT-0500 (Colombia Standard Time)",
                        "billed": 0.315
                    }""")
        );

        Mono<Void> response = notificationServiceImpl.sendNotification(notification);
        StepVerifier.create(response)
                .verifyComplete();

        RecordedRequest request = mockBackEnd.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("/alfa", request.getPath());
    }

    @SneakyThrows
    @Test
    @DisplayName("Performs handling of notification error with alfa.")
    void shouldHandleNotificationErrorOnAlfa() {

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.TOO_MANY_REQUESTS.value())
                .setBody("{}")
        );

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{}")
        );

        Mono<Void> response = notificationServiceImpl.sendNotification(notification);
        StepVerifier.create(response)
                        .verifyComplete();

        RecordedRequest request = mockBackEnd.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("/alfa", request.getPath());

        RecordedRequest request2 = mockBackEnd.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request2);
        assertEquals("/beta", request2.getPath());
    }
}