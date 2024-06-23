package co.com.dojo.api;

import co.com.dojo.model.MoneyTransfer;
import co.com.dojo.usecase.transfermoney.TransferMoneyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {
    private  final TransferMoneyUseCase transferMoneyUseCase;

    public Mono<ServerResponse> listenPOSTUseCase(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(MoneyTransfer.class)
            .flatMap(transferMoneyUseCase::sendMoney)
            .then(Mono.defer(() -> ServerResponse.ok().bodyValue("{\"message\":\"Transferencia exitosa\"}")));
    }
}
