package se.tink.backend.core;

import com.datastax.driver.core.utils.UUIDs;
import java.util.Date;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.uuid.UUIDUtils;

@Table(value = "notifications_events")
public class NotificationEvent {

    public static class Source {
        public static final String ACTIVITY_GENERATOR_WORKER_SAVE_ALL= "activity-generator-worker-save-all";
        public static final String MARK_AS_SENT = "mark-as-sent";
        public static final String MARK_AS_RECEIVED = "mark-as-received";
        public static final String MARK_ALL_AS_READ = "mark-all-as-read";
        public static final String SET_STATUS_BY_ID = "set-status-by-id";
        public static final String NOTIFICATION_GATEWAY_SAVE_ALL = "notification-gateway-save-all";
    }

    private Date date;
    private Date generated;
    private Date inserted;
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID notificationId;
    private boolean groupable;
    private String key;
    private String message;
    private String status;
    private String title;
    private String type;
    private String url;
    private String eventSource;

    public NotificationEvent() {
    }

    public NotificationEvent(Notification notification, String eventSource) {
        id = UUIDs.timeBased();
        inserted = new Date();

        date = notification.getDate();
        generated = notification.getGenerated();
        userId = UUIDUtils.fromTinkUUID(notification.getUserId());
        notificationId = UUIDUtils.fromTinkUUID(notification.getId());
        groupable = notification.isGroupable();
        key = notification.getKey();
        message = notification.getMessage();
        setStatus(notification.getStatus());
        title = notification.getTitle();
        type = notification.getType();
        url = notification.getUrl();

        this.eventSource = eventSource;
    }

    public Date getDate() {
        return date;
    }

    public Date getGenerated() {
        return generated;
    }

    public UUID getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public NotificationStatus getStatus() {
        if (status == null) {
            return null;
        }
        return NotificationStatus.valueOf(status);
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isGroupable() {
        return groupable;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setGenerated(Date generated) {
        this.generated = generated;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setGroupable(boolean groupable) {
        this.groupable = groupable;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(NotificationStatus status) {
        if (status == null) {
            this.status = null;
        } else {
            this.status = status.name();
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }

    public Date getInserted() {
        return inserted;
    }

    public String getEventSource() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }
}
