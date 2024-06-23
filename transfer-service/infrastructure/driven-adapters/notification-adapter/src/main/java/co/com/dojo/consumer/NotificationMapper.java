package co.com.dojo.consumer;

import co.com.dojo.consumer.dto.alfa.AlfaNotificationRequest;
import co.com.dojo.consumer.dto.beta.BetaNotificationRequest;
import co.com.dojo.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "phoneNumber", source = "mobile")
    @Mapping(target = "message", source = "text")
    Notification toModel(AlfaNotificationRequest alfaNotificationRequest);

    @Mapping(target = "phoneNumber", source = "destination")
    Notification toModel(BetaNotificationRequest notificationRequest);

    @Mapping(target = "mobile", source = "phoneNumber")
    @Mapping(target = "text", source = "message")
    AlfaNotificationRequest forAlfa(Notification notification);

    @Mapping(target = "destination", source = "phoneNumber")
    BetaNotificationRequest forBeta(Notification notification);
}
