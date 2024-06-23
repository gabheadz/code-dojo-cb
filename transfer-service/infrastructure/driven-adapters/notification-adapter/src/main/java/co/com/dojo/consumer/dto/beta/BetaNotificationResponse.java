package co.com.dojo.consumer.dto.beta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BetaNotificationResponse {

    private String id;
    private String destination;
    private String timestamp;
    private double cost;

}