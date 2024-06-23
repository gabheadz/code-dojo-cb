package co.com.dojo.usecase.transfermoney;

import co.com.dojo.model.MoneyTransfer;
import co.com.dojo.model.Notification;
import co.com.dojo.model.gateways.DepositsGateway;
import co.com.dojo.model.gateways.NotificationGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;

@RequiredArgsConstructor
public class TransferMoneyUseCase {

    private final DepositsGateway depositsGateway;
    private final NotificationGateway notificationGateway;

    public Mono<Void> sendMoney(MoneyTransfer moneyTransfer) {
        return depositsGateway.sendMoney(moneyTransfer)
                .doOnNext(mt -> this.notifyTransfer(mt).subscribe())
                .then();
    }

    private Mono<Void> notifyTransfer(MoneyTransfer moneyTransfer) {
        return Mono.fromSupplier(() ->
                Arrays.asList(moneyTransfer.getDestinationAccount(), moneyTransfer.getOriginAccount())
            )
            .delayElement(Duration.ofMillis(2_000)) // Simulate delay
            .flatMapMany(Flux::fromIterable)
            .flatMap(depositsGateway::getCustomerDetails)
            .map(customer -> Notification.builder()
                .phoneNumber(customer.getPhoneNumber())
                .message("Transferencia realizada por " + moneyTransfer.getAmount())
                .build()
            )
            .flatMap(notificationGateway::sendNotification)
            .then();
    }
}
