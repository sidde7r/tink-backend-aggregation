package se.tink.backend.common.utils;

import com.google.api.client.util.Base64;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.crypto.SecretKey;
import se.tink.backend.common.workers.notifications.channels.encryption.EncryptedPushNotification;
import se.tink.backend.common.workers.notifications.channels.encryption.EncryptedPushNotificationContainer;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.NotificationStatus;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NotificationUtils {

    private static final ImmutableSet<NotificationStatus> SENT_STATUSES = ImmutableSet
            .of(NotificationStatus.SENT, NotificationStatus.SENT_ENCRYPTED);

    public static boolean shouldSendEncrypted(Cluster cluster) {
        // ABN AMRO only.
        return Objects.equals(cluster, Cluster.ABNAMRO);
    }

    public static EncryptedPushNotificationContainer getEncryptedPushNotification(Notification notification,
            Device device) throws Exception {

        // No public key available (i.e. key encryption will not be able to be performed).
        if (Strings.isNullOrEmpty(device.getPublicKey())) {
            return null;
        }

        EncryptedPushNotification encryptedNotification = getEncryptedPushNotification(notification);

        // Serialize the notification.
        String payload = SerializationUtils.serializeToString(encryptedNotification);

        // Generate symmetric key.
        SecretKey aesKey = EncryptionUtils.AES.generateSecretKey();

        // Get the correct IvMode specification mode that is supported by the device.
        EncryptionUtils.AES.IvMode ivMode = AbnAmroUserAgentUtils
                .getPushNotificationsAESIvSpecificationMode(device.getUserAgent());

        // Encrypt the payload with the symmetric key.
        String encryptedPayload = EncryptionUtils.AES.encrypt(payload, aesKey, ivMode);

        // Get the public key format
        EncryptionUtils.RSA.PublicKeyFormat format = AbnAmroUserAgentUtils.getPublicKeyFormat(device.getUserAgent());

        // Initialize public (asymmetric) key.
        RSAPublicKey rsaPublicKey = EncryptionUtils.RSA.getPublicKey(device.getPublicKey(), format);

        // Encrypt the symmetric key with the public (asymmetric) key.
        String aesKeyBase64 = Base64.encodeBase64String(aesKey.getEncoded());
        String rsaEncryptedAesKey = EncryptionUtils.RSA.encrypt(aesKeyBase64, rsaPublicKey);

        EncryptedPushNotificationContainer encryptedNotificationContainer = new EncryptedPushNotificationContainer();
        encryptedNotificationContainer.setKey(rsaEncryptedAesKey);
        encryptedNotificationContainer.setPayload(encryptedPayload);
        return encryptedNotificationContainer;
    }

    public static EncryptedPushNotification getEncryptedPushNotification(Notification notification) {
        String title = notification.getSensitiveTitle();
        String message = notification.getSensitiveMessage();

        if (Strings.isNullOrEmpty(title)) {
            title = notification.getTitle();
        }

        if (Strings.isNullOrEmpty(message)) {
            message = notification.getMessage();
        }

        EncryptedPushNotification encryptedNotification = new EncryptedPushNotification();
        encryptedNotification.setId(notification.getId());
        encryptedNotification.setTitle(title);
        encryptedNotification.setMessage(message);
        encryptedNotification.setUrl(notification.getUrl());

        return encryptedNotification;
    }

    /**
     * Get the notification status depending on the input parameters. Read has "higher" prio than "sent".
     */
    public static Optional<NotificationStatus> getNotificationStatus(boolean isRead, boolean isSent,
            boolean encrypted) {

        if (isRead) {
            return Optional.of(NotificationStatus.READ);
        } else if (isSent) {
            return encrypted ? Optional.of(NotificationStatus.SENT_ENCRYPTED) : Optional.of(NotificationStatus.SENT);
        }

        return Optional.empty();
    }

    public static List<Notification> filterSendableNotifications(List<Notification> notifications) {
        return FluentIterable.from(notifications)
                .filter(notification ->
                        notification.getStatus() != null && SENT_STATUSES.contains(notification.getStatus())).toList();
    }
}
