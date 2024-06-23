package co.com.dojo.depositsmock;

import co.com.dojo.model.Customer;
import co.com.dojo.model.MoneyTransfer;
import co.com.dojo.model.gateways.DepositsGateway;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Random;

/**
 * Esta es una implementacion de un adaptador para un servicio core y muy ficticio de depositos.
 */
@Service
@Log
public class DepositsGatewayImpl implements DepositsGateway {

    private final Random random = new Random();

    /**
     * Metodo que permite realizar una transferencia de dinero. La implementacion de este metodo
     * es un mock.
     *
     * @param moneyTransfer el objeto que contiene la informacion de la transferencia.
     * @return un Mono con la transferencia realizada.
     */
    @Override
    public Mono<MoneyTransfer> sendMoney(MoneyTransfer moneyTransfer) {
        return Mono.defer(() -> Mono.just(moneyTransfer))
                .doOnSuccess(mt -> log.info("Transferencia realizada exitosamente, monto: " + mt.getAmount()));
    }

    /**
     * Metodo que permite obtener los detalles de un cliente a partir de un numero de cuenta.
     * @param accountNumber el numero de cuenta
     * @return un Mono con los detalles del cliente.
     */
    @Override
    public Mono<Customer> getCustomerDetails(String accountNumber) {
        return Mono.fromSupplier(() -> {
            String phoneNumber = String.format("%010d", random.nextInt(1_000_000_000));
            // Instrumentacion del mock. Cuando la cuenta termina en 99, el telefono del cliente
            // terminara en el numero 5, eso lo usaremos a nivel del api de notificaciones para
            // forzar un error.
            if (accountNumber.endsWith("99")) {
                phoneNumber += "5";
            }
            return Customer.
                    builder()
                    .accountNumber(accountNumber)
                    .phoneNumber(phoneNumber)
                    .name("Pedro Perez")
                    .build();
        })
        .doOnNext(mt -> log.info("Detalles del cliente de la cuenta: " + accountNumber + " obtenidos correctamente"));
    }



}
