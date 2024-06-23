package co.com.dojo.api;

import co.com.dojo.model.MoneyTransfer;
import co.com.dojo.usecase.transfermoney.TransferMoneyUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    TransferMoneyUseCase useCase;

    @Test
    void testListenPOSTUseCase() {

        when(useCase.sendMoney(any(MoneyTransfer.class)))
                .thenAnswer(invocation -> {
                    MoneyTransfer mt = invocation.getArgument(0);
                    return Mono.defer(() -> Mono.just(mt));
                });

        webTestClient.post()
                .uri("/api/money/send")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "originAccount": "123123123199",
                          "destinationAccount": "98979789799",
                          "amount": 1000.0
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(userResponse -> {
                            Assertions.assertThat(userResponse).isEqualTo("{\"message\":\"Transferencia exitosa\"}");
                        }
                );
    }
}
