package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "notifications")
@IdClass(NotificationPk.class)
public class Notification {
    @ApiModelProperty(value = "The date for which the notification was generated", example = "1455740874875", required = true)
    @Column(nullable = false)
    private Date date;
    // Quoted because 'generated' is a keyword in MySQL 5.7, and Hibernation
    // currently does not quote field names.
    @Column(name = "`generated`")
    @ApiModelProperty(value = "The date when the notification was generated", example = "1455740874875", required = true)
    private Date generated;
    @Id
    @Column(name = "`userid`")
    @ApiModelProperty(name = "userId", hidden = true)
    private String userId;
    @Id
    @Column(name = "`id`")
    @ApiModelProperty(name = "id", hidden = true)
    private String id;
    @ApiModelProperty(value = "Flag indicating whether or not the notification is groupable.", example = "true", required = true)
    private boolean groupable = true;
    @Column(name = "`notificationkey`")
    @ApiModelProperty(value = "The identifying key.", example = "unusual-category-high.2016-05.18bb1f4636894f3bba8ddcd567d22fbd", required = true)
    private String key;
    @Type(type = "text")
    @ApiModelProperty(value = "The notification message.", example = "You have spent more than usual on restaurants this month.", required = true)
    private String message;
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(value = "The notification status.", allowableValues = NotificationStatus.DOCUMENTED, example = "READ", required = true)
    private NotificationStatus status;
    @ApiModelProperty(value = "The notification title. Used on Android as title and concatenated with the message on iOS.", example = "More than usual", required = true)
    private String title;
    @ApiModelProperty(value = "The notification type", example = "unusual-category-high", required = true)
    private String type;
    @ApiModelProperty(value = "The deep-link URL", example = "tink://transactions/953c4eda24554a61a9653a479e70fc96", required = true)
    private String url;
    @ApiModelProperty(value = "The notification title if the notification is delivered encrypted. Used on Android as title and concatenated with the message on iOS.", example = "Expense", required = true)
    private String sensitiveTitle;
    @Type(type = "text")
    @ApiModelProperty(value = "The notification message if the notification is delivered encrypted.", example = "You had an expense charged by H&M.", required = true)
    private String sensitiveMessage;

    /**
     * The constructor is not supposed to be used in production code.
     * Use parameterised constructor(s) instead to avoid creating
     * notifications in invalid state. Package private access is needed
     * for Hibernate to be able to instantiate entities.
     */
    @SuppressWarnings("WeakerAccess")
    Notification() {
        id = StringUtils.generateUUID();
    }

    public Notification(@Nonnull String userId) {
        this();
        this.userId = userId;
        this.status = NotificationStatus.CREATED;
    }

    public Date getDate() {
        return date;
    }

    public Date getGenerated() {
        return generated;
    }

    public String getId() {
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
        return status;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getUserId() {
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

    public void setId(String id) {
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
        this.status = status;
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

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSensitiveTitle() {
        return sensitiveTitle;
    }

    public void setSensitiveTitle(String sensitiveTitle) {
        this.sensitiveTitle = sensitiveTitle;
    }

    public String getSensitiveMessage() {
        return sensitiveMessage;
    }

    public void setSensitiveMessage(String sensitiveMessage) {
        this.sensitiveMessage = sensitiveMessage;
    }

    public final static class Builder {
        private String key;
        private String userId;
        private Date date;
        private Date generated;
        private String title;
        private String message;
        private String sensitiveTitle;
        private String sensitiveMessage;
        private String url;
        private String type;
        private boolean groupable;

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder userId(@Nonnull String userId) {
            this.userId = userId;
            return this;
        }

        public Builder generated(Date generated) {
            this.generated = generated;
            return this;
        }

        public Builder date(@Nonnull Date date) {
            this.date = date;
            return this;
        }

        public Builder title(@Nonnull String title) {
            this.title = title;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder sensitiveTitle(@Nonnull String sensitiveTitle) {
            this.sensitiveTitle = sensitiveTitle;
            return this;
        }

        public Builder sensitiveMessage(String sensitiveMessage) {
            this.sensitiveMessage = sensitiveMessage;
            return this;
        }

        public Builder url(@Nonnull String url) {
            this.url = url;
            return this;
        }

        public Builder type(@Nonnull String type) {
            this.type = type;
            return this;
        }

        public Builder groupable(boolean groupable) {
            this.groupable = groupable;
            return this;
        }

        public Builder fromActivity(Activity activity) {
            this.userId = activity.getUserId();
            this.date = activity.getDate();
            this.message = activity.getMessage();
            this.key = activity.getKey();
            this.type = activity.getType();
            this.title = activity.getTitle();
            this.sensitiveMessage = activity.getSensitiveMessage();
            return this;
        }

        /**
         * Constructs a new {@link Notification}. Guarantees the notification to be valid if succeeds.
         *
         * @return a valid {@link Notification}
         * @throws IllegalArgumentException if build notification will not be valid
         */
        public Notification build() {
            Notification notification = new Notification(userId);
            notification.setKey(key);
            notification.setDate(date);
            notification.setGenerated(generated);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setUrl(url);
            notification.setType(type);
            notification.setGroupable(groupable);
            notification.setSensitiveTitle(sensitiveTitle);
            notification.setSensitiveMessage(sensitiveMessage);
            notification.setStatus(NotificationStatus.CREATED);

            if (!notification.isValid()) {
                throw new IllegalArgumentException("Constructed notification is invalid");
            }
            return notification;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("key", key)
                    .add("userId", userId)
                    .add("date", date)
                    .add("generated", generated)
                    .add("title", title)
                    .add("message", message)
                    .add("sensitive-title", sensitiveTitle)
                    .add("sensitive-message", sensitiveMessage)
                    .add("url", url)
                    .add("type", type)
                    .add("groupable", groupable)
                    .toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Notification that = (Notification) o;
        return groupable == that.groupable
                && Objects.equals(date, that.date)
                && Objects.equals(generated, that.generated)
                && Objects.equals(userId, that.userId)
                && Objects.equals(key, that.key)
                && Objects.equals(message, that.message)
                && status == that.status
                && Objects.equals(title, that.title)
                && Objects.equals(type, that.type)
                && Objects.equals(url, that.url)
                && Objects.equals(sensitiveMessage, that.sensitiveMessage)
                && Objects.equals(sensitiveTitle, that.sensitiveTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date,
                generated,
                userId,
                groupable,
                key,
                message,
                status,
                title,
                type,
                url,
                sensitiveTitle,
                sensitiveMessage);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("type", type)
                .add("key", key)
                .add("title", title)
                .add("message", message)
                .add("url", url).toString();
    }

    /**
     * Check that all required fields are set and valid.
     */
    @JsonIgnore
    public boolean isValid() {
        return this.userId != null
                && date != null
                && !Strings.isNullOrEmpty(title)
                && !Strings.isNullOrEmpty(url)
                && type != null;
    }
}
