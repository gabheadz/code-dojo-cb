package co.com.dojo.consumer.config;

import io.micrometer.observation.ObservationRegistry;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
public class ConsumerConfig {

    private final String alfaUrl;
    private final int alfaTimeout;
    private final String betaUrl;
    private final int betaTimeout;
    private final ObservationRegistry observationRegistry;

    public ConsumerConfig(@Value("${adapter.alfaservice.url}") String alfaUrl,
                          @Value("${adapter.alfaservice.timeout}") int alfaTimeout,
                          @Value("${adapter.betaservice.url}") String betaUrl,
                          @Value("${adapter.betaservice.timeout}") int betaTimeout,
                          ObservationRegistry observationRegistry) {
        this.alfaUrl = alfaUrl;
        this.alfaTimeout = alfaTimeout;
        this.betaUrl = betaUrl;
        this.betaTimeout = betaTimeout;
        this.observationRegistry = observationRegistry;
    }

    @Bean(name = "alfaWebClient")
    public WebClient alfaWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl(alfaUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .clientConnector(getClientHttpConnector(alfaTimeout))
            .observationRegistry(observationRegistry)
            .build();
    }

    @Bean(name = "betaWebClient")
    public WebClient betaWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(betaUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .clientConnector(getClientHttpConnector(betaTimeout))
                .observationRegistry(observationRegistry)
                .build();
    }

    private ClientHttpConnector getClientHttpConnector(int timeout) {
        return new ReactorClientHttpConnector(HttpClient.create()
                .compress(true)
                .keepAlive(true)
                .option(CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, MILLISECONDS));
                }));
    }

}
