package co.com.dojo.consumer.dto.alfa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AlfaNotificationResponse {

    private String mobile;
    private String timestamp;
    private double billed;

}