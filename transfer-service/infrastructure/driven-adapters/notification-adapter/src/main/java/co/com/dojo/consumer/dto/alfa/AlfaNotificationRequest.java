package co.com.dojo.consumer.dto.alfa;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AlfaNotificationRequest {
    private String mobile;
    private String text;

}
