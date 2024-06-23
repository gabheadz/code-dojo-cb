package co.com.dojo.depositsmock;

import co.com.dojo.model.MoneyTransfer;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DepositsGatewayImplTest {

    DepositsGatewayImpl depositsGatewayImpl = new DepositsGatewayImpl();

    @Test
    void testGetCustomerDetails() {
        // Just a simple test since the DepositsGatewayImpl is just a mock
        StepVerifier.create(depositsGatewayImpl.getCustomerDetails("1234"))
                .expectSubscription()
                .expectNextMatches(customer -> {
                    assertNotNull(customer);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testSendMoney() {
        // Just a simple test since the DepositsGatewayImpl is just a mock

        MoneyTransfer mt = MoneyTransfer.builder()
                .amount(1000.0d)
                .originAccount("1234")
                .destinationAccount("5678")
                .build();

        StepVerifier.create(depositsGatewayImpl.sendMoney(mt))
                .expectSubscription()
                .expectNextMatches(mt2 -> {
                    assertEquals(mt, mt2);
                    return true;
                })
                .verifyComplete();
    }
}
