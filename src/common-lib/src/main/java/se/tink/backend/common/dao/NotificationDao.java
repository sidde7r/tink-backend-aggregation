package se.tink.backend.common.dao;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.joda.time.DateTime;
import rx.Observable;
import se.tink.backend.common.repository.cassandra.NotificationEventRepository;
import se.tink.backend.common.repository.mysql.main.NotificationRepository;
import se.tink.backend.common.utils.repository.PrefixRepository;
import se.tink.backend.common.utils.repository.RepositoryUtils;
import se.tink.backend.core.Notification;
import se.tink.backend.core.NotificationEvent;
import se.tink.backend.core.NotificationStatus;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.metrics.MeterFactory;
import se.tink.libraries.metrics.MetricId;

public class NotificationDao {

    private static final int SAVE_BATCH_SIZE = 100;
    private static final LogUtils log = new LogUtils(NotificationDao.class);
    private static final MetricId NOTIFICATIONS_STATUS_CHANGE = MetricId.newId("notifications_status_change");
    private static final MetricId NOTIFICATIONS_RECEIVED_DURATION = MetricId.newId("notifications_received_duration");
    private static final int READ_BATCH_SIZE = 10000;
    private final NotificationRepository notificationRepository;
    private final NotificationEventRepository notificationEventRepository;
    private final MeterFactory meterFactory;

    @Inject
    public NotificationDao(NotificationRepository notificationRepository,
            NotificationEventRepository notificationEventRepository, MeterFactory meterFactory) {

        this.notificationRepository = notificationRepository;
        this.notificationEventRepository = notificationEventRepository;
        this.meterFactory = meterFactory;
    }

    public Notification findById(String id) {
        return notificationRepository.findById(id);
    }

    public List<Notification> findByUserId(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public void markAsSent(Collection<Notification> notifications, boolean encrypted) {

        for (Notification notification : notifications) {
            NotificationStatus oldStatus = notification.getStatus();
            notification.setStatus(encrypted ? NotificationStatus.SENT_ENCRYPTED : NotificationStatus.SENT);

            trackNotificationStatusChanged(notification, oldStatus);
        }

        save(notifications, NotificationEvent.Source.MARK_AS_SENT);
    }

    public void markAsReceived(Collection<Notification> notifications) {
        for (Notification notification : notifications) {
            NotificationStatus oldStatus = notification.getStatus();
            notification.setStatus(NotificationStatus.RECEIVED);

            trackNotificationStatusChanged(notification, oldStatus);
        }

        save(notifications, NotificationEvent.Source.MARK_AS_RECEIVED);
    }

    public void markAllAsReadByUserId(String userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);

        List<Notification> notificationsToStore = Lists.newArrayList();
        for (Notification notification : notifications) {
            NotificationStatus status = notification.getStatus();
            if (!Objects.equals(status, NotificationStatus.READ)) {
                notification.setStatus(NotificationStatus.READ);
                notificationsToStore.add(notification);
            }
        }

        save(notificationsToStore, NotificationEvent.Source.MARK_ALL_AS_READ);
    }

    /**
     * Save notifications (in batch of 4000,
     * since Datastax only allows maximum unsigned short size (65535) number of values in insertion
     * and there are 14 fields in the Notification Events table).
     */
    public void save(Collection<Notification> notifications, String eventSource) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }

        List<Notification> notificationsToStore = Lists.newArrayList();
        List<NotificationEvent> notificationEventsToStore = Lists.newArrayList();

        for (Notification notification : notifications) {
            notificationsToStore.add(notification);
            notificationEventsToStore.add(new NotificationEvent(notification, eventSource));
        }

        for (List<Notification> notificationsBatch : Lists.partition(notificationsToStore, SAVE_BATCH_SIZE)) {
            notificationRepository.save(notificationsBatch);
        }

        for (List<NotificationEvent> eventsBatch : Lists.partition(notificationEventsToStore, SAVE_BATCH_SIZE)) {
            notificationEventRepository.save(eventsBatch);
        }
    }

    public void delete(Iterable<Notification> notifications) {
        if (notifications == null || Iterables.isEmpty(notifications)) {
            return;
        }

        notificationRepository.delete(notifications);
    }

    public void setStatusById(String id, NotificationStatus status) {
        Notification notification = notificationRepository.findById(id);

        if (notification == null) {
            log.warn(String.format("Notification not found (Id = '%s')", id));
            return;
        }

        NotificationStatus oldStatus = notification.getStatus();
        notification.setStatus(status);

        save(Lists.newArrayList(notification), NotificationEvent.Source.SET_STATUS_BY_ID);

        trackNotificationStatusChanged(notification, oldStatus);

        if (Objects.equals(NotificationStatus.RECEIVED, status)) {
            trackReceivedNotification(notification, oldStatus);
        }
    }

    /**
     * Report the status change of a notification (for example that the status was changed from "CREATED" to "SENT".
     */
    private void trackNotificationStatusChanged(Notification notification, NotificationStatus oldStatus) {
        log.debug(notification.getUserId(),
                String.format("Notification status changed (Id = '%s', From = '%s', To = '%s')",
                        notification.getId(), oldStatus, notification.getStatus()));

        MetricId metricId = NOTIFICATIONS_STATUS_CHANGE
                .label("from", getLabel(oldStatus))
                .label("to", getLabel(notification.getStatus()));

        meterFactory.getCounter(metricId).inc();
    }

    /**
     * Report the duration from when the notification was generated until we got the acknowledge from clients.
     */
    private void trackReceivedNotification(Notification notification, NotificationStatus oldStatus) {
        if (notification.getGenerated() == null) {
            log.error("Notification doesn't have a generated date.");
            return;
        }

        long duration = new DateTime().getMillis() - notification.getGenerated().getTime();

        log.debug(notification.getUserId(),
                String.format("Notification Received (Id = '%s', Status = '%s', Duration = '%s')",
                        notification.getId(), oldStatus, DateUtils.prettyFormatMillis((int) duration)));

        MetricId metricId = NOTIFICATIONS_RECEIVED_DURATION.label("status", getLabel(oldStatus));

        meterFactory.getHistogram(metricId).update(duration);
    }

    private static String getLabel(NotificationStatus notificationStatus) {
        return notificationStatus == null ? "null" : notificationStatus.toString();
    }

    public List<Notification> findRandomUnsent(int batchSize) {
        return notificationRepository.findRandomUnsent(batchSize);
    }

    public List<Notification> findAllByUserIdAndKey(String userId, String key) {
        return notificationRepository.findAllByUserIdAndKey(userId, key);
    }

    public List<Notification> findAllByStatus(NotificationStatus status, String prefix) {
        return notificationRepository.findAllByStatus(status, prefix);
    }

    public void deleteByUserId(String userId) {
        notificationRepository.deleteByUserId(userId);
    }

    public Observable<Notification> streamByUserWithStatus(NotificationStatus status) {
        Observable<Observable<Notification>> notificationsNestedObservable = Observable.create(t -> {
            try {

                // NOTE: Any database logic in this anonymous method must be refactored out to a separate method in
                // UserRepositoryImpl to make sure that each database operation uses a non-closed database session.

                for (String useridPrefix : RepositoryUtils.hexPrefixes(10)) {
                    if (t.isUnsubscribed()) {
                        return;
                    }
                    log.trace("Querying for prefix: " + useridPrefix);
                    t.onNext(Observable.from(this.findAllByStatus(status, useridPrefix)));
                }

                if (!t.isUnsubscribed()) {
                    t.onCompleted();
                }
            } catch (Throwable e) {
                if (!t.isUnsubscribed()) {
                    t.onError(e);
                }
            }
        });
        return Observable.concat(notificationsNestedObservable);
    }


}
