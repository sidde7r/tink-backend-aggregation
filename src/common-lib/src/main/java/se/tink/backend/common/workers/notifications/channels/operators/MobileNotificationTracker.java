package se.tink.backend.common.workers.notifications.channels.operators;

import com.google.common.base.Preconditions;
import java.util.List;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.concurrency.ListenableExecutor;
import se.tink.backend.common.repository.cassandra.EventRepository;
import se.tink.backend.core.Device;
import se.tink.backend.core.Event;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

public class MobileNotificationTracker implements MobileNotificationOperator {
    private static final LogUtils log = new LogUtils(MobileNotificationTracker.class);
    private final ListenableExecutor<Runnable> trackingExecutorService;
    private final EventRepository eventRepository;
    private final MobileNotificationOperator nextOperator;

    public MobileNotificationTracker(MobileNotificationOperator nextOperator, ServiceContext context) {
        trackingExecutorService = context.getTrackingExecutorService();
        eventRepository = context.getRepository(EventRepository.class);
        this.nextOperator = Preconditions.checkNotNull(nextOperator);
    }

    @Override
    public void process(Notification notification, List<Device> devices, User user, boolean encrypted,
            int unreadNotifications) {
        String format;

        if (encrypted) {
            format = "encrypted-notification.%s";
        } else {
            format = "notification.%s";
        }

        String trackingName = String.format(format, notification.getType());
        log.info(user.getId(), "Event: " + trackingName);

        final Event finalEventToPersist = new Event(user.getId(), trackingName);

        trackingExecutorService.execute(() -> eventRepository.save(finalEventToPersist));

        nextOperator.process(notification, devices, user, encrypted, unreadNotifications);
    }
}
