package se.tink.backend.system.notifications;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.backend.common.config.NotificationsConfiguration;
import se.tink.backend.common.dao.NotificationDao;
import se.tink.backend.common.utils.ExecutorServiceUtils;
import se.tink.backend.common.workers.notifications.channels.MobileNotificationSender;
import se.tink.backend.core.Notification;
import se.tink.backend.core.NotificationEvent;
import se.tink.backend.core.NotificationSettings;
import se.tink.backend.core.User;
import se.tink.backend.system.rpc.SendNotificationsRequest;
import se.tink.backend.system.rpc.UserNotificationsContainer;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricRegistry;

public class NotificationGateway implements Managed {
    private static final LogUtils log = new LogUtils(NotificationGateway.class);
    private final NotificationsConfiguration notificationsConfiguration;
    private MobileNotificationSender notificationSender;
    private final MetricRegistry metricRegistry;

    private ListenableThreadPoolExecutor<Runnable> notificationsSenderExecutorService;

    private final NotificationDao notificationDao;
    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("notifications-sender-%d")
            .build();

    @Inject
    public NotificationGateway(MobileNotificationSender notificationSender,
            NotificationDao notificationDao, NotificationsConfiguration notificationsConfiguration,
            MetricRegistry metricRegistry) {
        this.notificationSender = notificationSender;
        this.notificationDao = notificationDao;
        this.notificationsConfiguration = notificationsConfiguration;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void start() throws Exception {
        notificationSender.start();

        LinkedBlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> executorServiceQueue = Queues.<WrappedRunnableListenableFutureTask<Runnable, ?>>newLinkedBlockingQueue();
        notificationsSenderExecutorService = ListenableThreadPoolExecutor.builder(
                executorServiceQueue,
                new TypedThreadPoolBuilder(10, threadFactory)
                        .withMaximumPoolSize(200, 5, TimeUnit.MINUTES))
                .withMetric(metricRegistry, "notifications_sender_executor_service")
                .build();
    }

    @Override
    public void stop() throws Exception {
        ExecutorServiceUtils.shutdownExecutor("NotificationGateway#notificationsSenderExecutorService",
                notificationsSenderExecutorService, 20, TimeUnit.SECONDS);
        notificationsSenderExecutorService = null;

        notificationSender.stop();
    }

    @VisibleForTesting
    public MobileNotificationSender getNotificationSender() {
        return notificationSender;
    }

    @VisibleForTesting
    public void setNotificationSender(MobileNotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    public static class FutureNotifications {
        public final User user;
        public final boolean encrypted;
        public final boolean track;
        public final ImmutableList<Notification> notification;

        public FutureNotifications(User user, boolean encrypted, boolean track, List<Notification> notification) {
            this.user = user;
            this.encrypted = encrypted;
            this.track = track;
            this.notification = ImmutableList.copyOf(notification);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FutureNotifications that = (FutureNotifications) o;
            return encrypted == that.encrypted &&
                    track == that.track &&
                    Objects.equal(user, that.user) &&
                    Objects.equal(notification, that.notification);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(user, encrypted, track, notification);
        }
    }

    public List<ListenableFuture<FutureNotifications>> sendNotifications(SendNotificationsRequest request) {
        if (request.getUserNotifications().isEmpty()) {
            return ImmutableList.of();
        }

        // 1. Save notifications.

        Map<String, List<Notification>> userNotificationsByUserId = Maps.newHashMap();

        for (UserNotificationsContainer userNotificationsContainer : request.getUserNotifications()) {

            // Only save the notification if the user has enabled that in profile.

            NotificationSettings notificationSettings = userNotificationsContainer.getUser().getProfile()
                    .getNotificationSettings();
            List<Notification> usersNotifications = Lists.newArrayList();

            for (Notification notification : userNotificationsContainer.getNotifications()) {

                notification.setUserId(userNotificationsContainer.getUser().getId());

                if (!notification.isValid()) {
                    log.warn(userNotificationsContainer.getUser().getId(),
                            "Notification is not valid: " + notification.toString());
                    continue;
                }
                if (notificationSettings.generateNotificationsForType(notification.getType())) {
                    usersNotifications.add(notification);
                }
            }

            userNotificationsByUserId.put(userNotificationsContainer.getUser().getId(), usersNotifications);
        }

        List<Notification> allNotifications = Lists.newArrayList();

        for (List<Notification> userNotifications : userNotificationsByUserId.values()) {
            allNotifications.addAll(userNotifications);
        }

        // TODO this call is unnecessary for those jobs when we are reading up existing notifications from database
        // and just sending them out.

        notificationDao.save(allNotifications, NotificationEvent.Source.NOTIFICATION_GATEWAY_SAVE_ALL);

        // 2. Send notifications (if appropriate).

        ArrayList<ListenableFuture<FutureNotifications>> result = Lists
                .newArrayListWithCapacity(request.getUserNotifications().size());

        for (final UserNotificationsContainer userNotificationsContainer : request.getUserNotifications()) {
            final User user = userNotificationsContainer.getUser();
            final List<Notification> notifications = userNotificationsByUserId.get(user.getId());

            if (notificationsConfiguration.shouldSendNotifications(user)) {
                if (notifications.size() > 0) {
                    final boolean encrypted = userNotificationsContainer.isEncrypted();

                    final boolean shouldBeTracked = true;
                    FutureNotifications notificationData = new FutureNotifications(user, encrypted, shouldBeTracked,
                            notifications);

                    ListenableFuture<FutureNotifications> future = notificationsSenderExecutorService
                            .execute(() -> {
                                try {

                                    // 2.a. Mark as sent. Doing this within the executor to limit the scope/time
                                    // between marking a notification as sent and actually sending it.
                                    notificationDao.markAsSent(notifications, encrypted);

                                    // 2.b. Send.
                                    notificationSender
                                            .sendNotifications(user, notifications, shouldBeTracked, encrypted);
                                } catch (Exception e) {
                                    log.error(user.getId(), "Could not send notifications.", e);
                                    throw e; // Rethrow for futures to know that errors happened.
                                }
                            }, notificationData);
                    result.add(future);
                }
            }
        }

        return result;
    }
}
