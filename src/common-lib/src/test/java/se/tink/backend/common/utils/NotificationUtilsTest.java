package se.tink.backend.common.utils;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.common.workers.notifications.channels.encryption.EncryptedPushNotification;
import se.tink.backend.core.Notification;
import se.tink.backend.core.NotificationStatus;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class NotificationUtilsTest {

    @Parameters({
            "true, true, true",
            "true, true, false",
            "true, false, true",
            "true, false, false",
    })
    @Test
    public void readIsFirstPriority(boolean isRead, boolean isSent, boolean isEncrypted) {
        assertThat(NotificationUtils.getNotificationStatus(isRead, isSent, isEncrypted).get())
                .isEqualTo(NotificationStatus.READ);
    }

    @Test
    public void sendIsSecondPriority() {
        assertThat(NotificationUtils.getNotificationStatus(false, true, true).get())
                .isEqualTo(NotificationStatus.SENT_ENCRYPTED);

        assertThat(NotificationUtils.getNotificationStatus(false, true, false).get())
                .isEqualTo(NotificationStatus.SENT);
    }

    @Parameters({
            "false, false, true",
            "false, false, false",
    })
    @Test
    public void absentIfNotReadAndNotSent(boolean isRead, boolean isSent, boolean isEncrypted) {
        assertThat(NotificationUtils.getNotificationStatus(isRead, isSent, isEncrypted).isPresent()).isFalse();
    }

    @Test
    public void filterSendableNotifications() {

        Notification notificationNoStatus = new Notification("userId");
        notificationNoStatus.setStatus(null);

        Notification notificationCreated = new Notification("userId");
        notificationCreated.setStatus(NotificationStatus.CREATED);

        Notification notificationRead = new Notification("userId");
        notificationRead.setStatus(NotificationStatus.READ);

        Notification notificationReceived = new Notification("userId");
        notificationReceived.setStatus(NotificationStatus.READ);

        Notification notificationSent = new Notification("userId");
        notificationSent.setStatus(NotificationStatus.SENT);

        Notification notificationSentEncrypted = new Notification("userId");
        notificationSentEncrypted.setStatus(NotificationStatus.SENT_ENCRYPTED);

        ImmutableList<Notification> notifications = ImmutableList
                .of(notificationNoStatus, notificationCreated, notificationRead, notificationReceived, notificationSent,
                        notificationSentEncrypted);

        assertThat(NotificationUtils.filterSendableNotifications(notifications))
                .containsExactly(notificationSent, notificationSentEncrypted);
    }

    @Test
    public void testCreateEncryptedNotificationWithoutSensitiveMessage() throws Exception {
        Notification notification = new Notification.Builder()
                .userId("userId")
                .key("key")
                .date(new Date())
                .generated(new Date())
                .title("not sensitive title")
                .message("not sensitive message")
                .url("url")
                .type("type")
                .build();

        EncryptedPushNotification encrypted = NotificationUtils.getEncryptedPushNotification(notification);

        assertThat(encrypted.getId()).isEqualTo(notification.getId());
        assertThat(encrypted.getUrl()).isEqualTo(notification.getUrl());
        assertThat(encrypted.getMessage()).isEqualTo(notification.getMessage());
        assertThat(encrypted.getTitle()).isEqualTo(notification.getTitle());
    }

    @Test
    public void testCreateEncryptedNotificationWithSensitiveMessage() throws Exception {
        Notification notification = new Notification.Builder()
                .userId("userId")
                .key("key")
                .date(new Date())
                .generated(new Date())
                .title("not sensitive title")
                .message("not sensitive message")
                .sensitiveMessage("sensitive message")
                .sensitiveTitle("sensitive title")
                .url("url")
                .type("type")
                .build();

        EncryptedPushNotification encrypted = NotificationUtils.getEncryptedPushNotification(notification);

        assertThat(encrypted.getId()).isEqualTo(notification.getId());
        assertThat(encrypted.getUrl()).isEqualTo(notification.getUrl());
        assertThat(encrypted.getMessage()).isEqualTo(notification.getSensitiveMessage());
        assertThat(encrypted.getTitle()).isEqualTo(notification.getSensitiveTitle());
    }
}
