package se.tink.backend.common.workers.notifications.channels.google;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import static org.assertj.core.api.Assertions.assertThat;

public class GooglePushNotificationRequestBuilderTest {

    @Test(expected = Exception.class)
    public void testExceptionIfDevicesIsNull() {

        GooglePushNotificationRequestBuilder builder = new GooglePushNotificationRequestBuilder();

        builder.withNotification(new Notification("userId")).build();
    }

    @Test(expected = Exception.class)
    public void testExceptionIfNotificationsIsNull() {

        GooglePushNotificationRequestBuilder builder = new GooglePushNotificationRequestBuilder();

        builder.withDevices(Lists.<Device>newArrayList()).build();
    }

    @Test(expected = Exception.class)
    public void testExceptionOnUserIdMismatch() {

        GooglePushNotificationRequestBuilder builder = new GooglePushNotificationRequestBuilder();

        Notification notification = new Notification("user-1");

        Device device = new Device();
        device.setUserId("user-2");
        device.setDeviceToken("token");

        builder.withNotification(notification).withDevices(Lists.newArrayList(device)).build();
    }

    @Test
    public void testNoExceptionIfCorrectUser() {

        GooglePushNotificationRequestBuilder builder = new GooglePushNotificationRequestBuilder();

        Notification notification = new Notification("user-1");

        Device device = new Device();
        device.setUserId("user-1");
        device.setNotificationToken("push-token");

        assertThat(builder.withNotification(notification).withDevices(Lists.newArrayList(device)).build()).hasSize(1);
    }

    @Test
    public void testPlainTextNotification() {

        GooglePushNotificationRequestBuilder builder = new GooglePushNotificationRequestBuilder();

        Notification notification = new Notification("user-1");
        notification.setMessage("message-1");
        notification.setKey("key-1");
        notification.setTitle("title-1");
        notification.setUrl("url-1");
        notification.setType("type-1");

        Device device = new Device();
        device.setUserId("user-1");
        device.setNotificationToken("push-token");
        device.setUserAgent("Grip/4.0.1 (iOS; 10.3.2, iPhone)");

        List<GooglePushNotificationRequest> requests = builder
                .withNotification(notification)
                .withDevices(Lists.newArrayList(device))
                .withEncryption(false)
                .build();

        assertThat(requests).hasSize(1);

        GooglePushNotificationRequest request = requests.get(0);

        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.KEY)).isEqualTo("key-1");
        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.MESSAGE)).isEqualTo("message-1");
        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.TITLE)).isEqualTo("title-1");
        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.TYPE)).isEqualTo("type-1");
        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.URL)).isEqualTo("url-1");
        assertThat(request.getPushTokens()).contains("push-token");

        // Should not have any encrypted content
        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.ENCRYPTED_CONTENT)).isNull();
    }

    @Test
    public void testEncryptedNotification() {

        GooglePushNotificationRequestBuilder builder = new GooglePushNotificationRequestBuilder();

        Notification notification = new Notification("user-1");
        notification.setMessage("message-1");
        notification.setKey("key-1");
        notification.setTitle("title-1");
        notification.setUrl("url-1");
        notification.setType("type-1");

        Device device = new Device();
        device.setUserId("user-1");
        device.setNotificationToken("push-token");
        device.setUserAgent("Grip/2.0.0 (iOS; 10.3.2, iPhone)");

        // Must be a valid key
        device.setPublicKey("PFJTQUtleVZhbHVlPjxNb2R1bHVzPi9mNlp6a1Y4OG96WTBnUURrVG1ndkNGYVZseTNxZDJUTnJFMjBjTkNC"
                + "TGJUSXp2Q1ZLbloxdjRnZ0ExVEwyR3ZwNjRTcXZjNTd5bzE5aENsVUNUTjl4RndzeTdDWXZYSUpRMlk0MnduZ1dqdnd1MU"
                + "xWMzNLK0FZY2k1dUFRS2VDVXVrMWpmQTVvMUZXcUFiQXlLUmpNaDF5NjM1UEtDdFpsWXBXQy9tRFB4OD08L01vZHVsdXM+"
                + "PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjwvUlNBS2V5VmFsdWU+");

        List<GooglePushNotificationRequest> requests = builder
                .withNotification(notification)
                .withDevices(Lists.newArrayList(device))
                .withEncryption(true)
                .build();

        assertThat(requests).hasSize(1);

        GooglePushNotificationRequest request = requests.get(0);

        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.KEY)).isNull();
        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.MESSAGE)).isNull();
        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.TITLE)).isNull();
        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.TYPE)).isNull();
        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.URL)).isNull();
        assertThat(request.getPushTokens()).contains("push-token");
        assertThat((request.getDevices()).contains(device));

        // Should have any encrypted content
        assertThat(request.getData().get(GooglePushNotificationRequest.Keys.ENCRYPTED_CONTENT)).isNotNull();
    }
}
