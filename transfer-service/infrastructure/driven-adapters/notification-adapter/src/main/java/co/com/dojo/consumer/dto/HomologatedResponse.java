package co.com.dojo.consumer.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomologatedResponse {
    private boolean success;
}
