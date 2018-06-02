package se.tink.backend.system.transports;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import java.util.List;
import java.util.concurrent.TimeUnit;
import se.tink.backend.system.api.NotificationGatewayService;
import se.tink.backend.system.notifications.NotificationGateway;
import se.tink.backend.system.rpc.SendNotificationsRequest;

public class NotificationTransport implements NotificationGatewayService, Managed {
    private final NotificationGateway notificationGateway;

    @Inject
    public NotificationTransport(NotificationGateway notificationGateway) {
        this.notificationGateway = notificationGateway;
    }

    @Override
    public void sendNotificationsAsynchronously(SendNotificationsRequest request) {
        notificationGateway.sendNotifications(request);
    }

    @Override
    public void sendNotificationsSynchronously(SendNotificationsRequest request) {

        try {
            // Will block until all notifications have been sent
            ListenableFuture<List<NotificationGateway.FutureNotifications>> future = Futures
                    .successfulAsList(notificationGateway.sendNotifications(request));

            Uninterruptibles.getUninterruptibly(future, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {
        notificationGateway.start();
    }

    @Override
    public void stop() throws Exception {
        notificationGateway.stop();
    }

}
