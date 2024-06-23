package co.com.dojo.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class MoneyTransfer {
    private String originAccount;
    private String destinationAccount;
    private Double amount;
}
