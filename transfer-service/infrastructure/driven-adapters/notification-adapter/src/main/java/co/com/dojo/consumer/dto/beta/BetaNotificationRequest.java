package co.com.dojo.consumer.dto.beta;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BetaNotificationRequest {
    private String destination;
    private String message;

}
