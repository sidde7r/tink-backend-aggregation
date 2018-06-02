package se.tink.backend.rpc;

import java.util.UUID;
import org.junit.Test;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;
import static org.assertj.core.api.Assertions.assertThat;

public class RegisterUserPushTokenCommandTest {

    @Test
    public void testCorrectConstruction() throws InvalidPin6Exception {
        String deviceId = UUID.randomUUID().toString();

        RegisterUserPushTokenCommand command = RegisterUserPushTokenCommand.builder()
                .withUserId("UserId")
                .withUserAgent("UserAgent")
                .withNotificationToken("NotificationToken")
                .withNotificationPublicKey("NotificationPublicKey")
                .withDeviceId(deviceId)
                .build();

        assertThat(command.getUserId()).isEqualTo("UserId");
        assertThat(command.getUserAgent()).isEqualTo("UserAgent");
        assertThat(command.getNotificationToken()).isEqualTo("NotificationToken");
        assertThat(command.getNotificationPublicKey()).isEqualTo("NotificationPublicKey");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingUserId() throws InvalidPin6Exception {
        RegisterUserPushTokenCommand command = RegisterUserPushTokenCommand.builder()
                .withUserAgent("UserAgent")
                .withNotificationToken("NotificationToken")
                .withNotificationPublicKey("NotificationPublicKey")
                .withDeviceId(UUID.randomUUID().toString())
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingUserAgent() throws InvalidPin6Exception {
        RegisterUserPushTokenCommand.builder()
                .withUserId("UserId")
                .withNotificationToken("NotificationToken")
                .withNotificationPublicKey("NotificationPublicKey")
                .withDeviceId(UUID.randomUUID().toString())
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingNotificationToken() throws InvalidPin6Exception {
        RegisterUserPushTokenCommand.builder()
                .withUserId("UserId")
                .withUserAgent("UserAgent")
                .withNotificationPublicKey("NotificationPublicKey")
                .withDeviceId(UUID.randomUUID().toString())
                .build();
    }

    @Test
    public void testRemoveWhitespacesFromPublicKey() throws InvalidPin6Exception {
        RegisterUserPushTokenCommand command = RegisterUserPushTokenCommand.builder()
                .withUserId("UserId")
                .withUserAgent("UserAgent")
                .withNotificationToken("NotificationToken")
                .withNotificationPublicKey("line1\n" + "line2\n" + "line3\n")
                .withDeviceId(UUID.randomUUID().toString())
                .build();

        assertThat(command.getNotificationPublicKey()).isEqualTo("line1line2line3");
    }
}
