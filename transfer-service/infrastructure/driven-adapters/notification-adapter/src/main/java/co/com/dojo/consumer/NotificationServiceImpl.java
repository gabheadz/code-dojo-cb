package co.com.dojo.consumer;

import co.com.dojo.consumer.dto.HomologatedResponse;
import co.com.dojo.consumer.dto.alfa.AlfaNotificationResponse;
import co.com.dojo.consumer.dto.beta.BetaNotificationResponse;
import co.com.dojo.model.Notification;
import co.com.dojo.model.gateways.NotificationGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;

@Service
@Log4j2
public class NotificationServiceImpl implements NotificationGateway {

    private final WebClient alfaWebClient;
    private final WebClient betaWebClient;
    private final NotificationMapper notificationMapper;
    private final ObservationRegistry observationRegistry;

    public NotificationServiceImpl(@Qualifier("alfaWebClient") WebClient alfaWebClient,
                                   @Qualifier("betaWebClient") WebClient betaWebClient,
                                   NotificationMapper notificationMapper,
                                   ObservationRegistry observationRegistry) {
        this.alfaWebClient = alfaWebClient;
        this.betaWebClient = betaWebClient;
        this.notificationMapper = notificationMapper;
        this.observationRegistry = observationRegistry;
    }

    /**
     * Metodo que envia una notificacion con un servicio externo. Este usa  el mecanismo
     * principal de notificacion, y en caso de fallar, se invoca un mecanismo de fallback
     * de manera manual.
     *
     * @param notification La notificacion a enviar.
     * @return Mono<Void> se asume que la operacion fue exitosa, de lo contrario sera una senal de
     * error.
     */
    @CircuitBreaker(name = "sendNotification", fallbackMethod = "alternativeSendNotification")
    public Mono<Void> sendNotification(Notification notification) {
        return Mono.fromSupplier(() -> notificationMapper.forAlfa(notification))
                .flatMap(notificationRequest -> alfaWebClient
                    .post()
                    .body(BodyInserters.fromValue(notificationRequest))
                    .retrieve()
                    .bodyToMono(AlfaNotificationResponse.class)
                )
                .doOnNext(response -> log.info("Notification sent, ALFA {}", response))
                .map(response -> HomologatedResponse.builder().success(true).build())
                .doOnError(throwable -> log.error("Error sending notification with Alfa", throwable))
                .name("send_notification_alfa")
                .tap(Micrometer.observation(observationRegistry))
                .then();
    }

    /**
     * Implementacion de un posible mecanismo de fallback invocando un segundo servicio de notificacion
     * mas costoso.
     *
     * @param notification La notificacion que no se pudo enviar con el primer servicio.
     * @return HomologatedResponse con el resultado de la notificacion.
     */
    public Mono<Void> alternativeSendNotification(Notification notification,
                                                  Exception ex) {
        return Mono.fromSupplier(() -> notificationMapper.forBeta(notification))
                .flatMap(notificationRequest -> betaWebClient
                        .post()
                        .body(BodyInserters.fromValue(notificationRequest))
                        .retrieve()
                        .bodyToMono(BetaNotificationResponse.class)
                )
                .doOnNext(response -> log.info("Notification sent, BETA {}", response))
                .map(response -> HomologatedResponse.builder().success(true).build())
                .doOnError(throwable -> log.error("Error sending notification with Beta", throwable))
                .name("send_notification_beta")
                .tap(Micrometer.observation(observationRegistry))
                .then();
    }

}
