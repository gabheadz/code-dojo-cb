package co.com.dojo.usecase.transfermoney;

import co.com.dojo.model.Customer;
import co.com.dojo.model.MoneyTransfer;
import co.com.dojo.model.Notification;
import co.com.dojo.model.gateways.DepositsGateway;
import co.com.dojo.model.gateways.NotificationGateway;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferMoneyUseCaseTest {

    @Mock
    DepositsGateway depositsGateway;

    @Mock
    NotificationGateway notificationGateway;

    @InjectMocks
    TransferMoneyUseCase transferMoneyUseCase;

    Random random = new Random();

    @SneakyThrows
    @Test
    void shouldTestSendMoney() {
        // Given
        MoneyTransfer moneyTransfer = new MoneyTransfer();
        moneyTransfer.setOriginAccount("100333001");
        moneyTransfer.setDestinationAccount("200555002");
        moneyTransfer.setAmount(1000.0d);

        // When
        when(depositsGateway.sendMoney(any(MoneyTransfer.class)))
                .thenAnswer(invocation -> {
                    MoneyTransfer param = invocation.getArgument(0);
                    return Mono.defer(() -> Mono.just(param));
                });

        when(depositsGateway.getCustomerDetails(anyString()))
                .thenAnswer(invocation -> {
                    String account = invocation.getArgument(0);
                    String phoneNumber = String.format("%010d", random.nextInt(1_000_000_000));
                    return Mono.defer(() -> Mono.just(Customer.builder()
                            .accountNumber(account)
                            .phoneNumber(phoneNumber)
                            .build()));
                });

        when(notificationGateway.sendNotification(any(Notification.class)))
                .thenReturn(Mono.empty().then());

        // Then
        StepVerifier.create(transferMoneyUseCase.sendMoney(moneyTransfer))
            .expectComplete()
            .verify();

        // Wait for side effects
        Thread.sleep(2200);

        verify(depositsGateway, times(2)).getCustomerDetails(anyString());
        verify(depositsGateway).sendMoney(any(MoneyTransfer.class));
        verify(notificationGateway, times(2)).sendNotification(any(Notification.class));
    }
}
