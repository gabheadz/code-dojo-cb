package co.com.dojo.model.gateways;

import co.com.dojo.model.Customer;
import co.com.dojo.model.MoneyTransfer;
import reactor.core.publisher.Mono;

public interface DepositsGateway {
    Mono<Customer> getCustomerDetails(String accountNumber);
    Mono<MoneyTransfer> sendMoney(MoneyTransfer moneyTransfer);
}
