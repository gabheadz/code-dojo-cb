package co.com.dojo.model.gateways;

import co.com.dojo.model.Notification;
import reactor.core.publisher.Mono;

public interface NotificationGateway {
    Mono<Void> sendNotification(Notification notification);
}
