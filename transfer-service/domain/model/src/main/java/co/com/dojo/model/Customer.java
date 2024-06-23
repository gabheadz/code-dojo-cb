package co.com.dojo.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Customer {
    private String id;
    private String name;
    private String phoneNumber;
    private String accountNumber;
}
