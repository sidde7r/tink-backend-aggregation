package se.tink.backend.common.workers.notifications.channels.google;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.core.Device;
import se.tink.backend.core.User;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GoogleExpiredPushNotificationHandlerTest {

    private DeviceRepository deviceRepository;
    private MetricRegistry metricRegistry;

    @Before
    public void setup() {
        deviceRepository = mock(DeviceRepository.class);
        metricRegistry = new MetricRegistry();
    }

    @Test
    public void testNoErrorsOnEmptyInputs() {
        GoogleExpiredPushNotificationHandler handler = new GoogleExpiredPushNotificationHandler(deviceRepository,
                metricRegistry);

        List<GooglePushNotificationResponseResult> results = Lists.newArrayList();
        List<Device> devices = Lists.newArrayList();
        User user = new User();

        handler.handle(results, devices, user, true);
    }

    @Test
    public void testDeviceIsRemovedIfTokenIsExpired() {
        GoogleExpiredPushNotificationHandler handler = new GoogleExpiredPushNotificationHandler(deviceRepository,
                metricRegistry);

        GooglePushNotificationResponseResult result1 = new GooglePushNotificationResponseResult();
        result1.setToken(null); // means that this is an valid token

        GooglePushNotificationResponseResult result2 = new GooglePushNotificationResponseResult();
        result2.setToken("valid-token"); // means that this is an invalid token

        Device device1 = new Device();
        device1.setId(1);
        device1.setNotificationToken("valid-token");

        Device device2 = new Device();
        device2.setNotificationToken("invalid-token");
        device2.setId(2);

        handler.handle(Lists.newArrayList(result1, result2), Lists.newArrayList(device1, device2),
                new User(), true);

        // We should remove device 2 since we have the token on device 1 already
        verify(deviceRepository).delete(Lists.newArrayList(device2));
    }

    @Test
    public void testDeviceIsSavedIfTokenIsExpired() {
        GoogleExpiredPushNotificationHandler handler = new GoogleExpiredPushNotificationHandler(deviceRepository,
                metricRegistry);

        GooglePushNotificationResponseResult result1 = new GooglePushNotificationResponseResult();
        result1.setToken(null); // means that this is an valid token

        GooglePushNotificationResponseResult result2 = new GooglePushNotificationResponseResult();
        result2.setToken("valid-token-2"); // means that this is an invalid token

        Device device1 = new Device();
        device1.setId(1);
        device1.setNotificationToken("valid-token-1");

        Device device2 = new Device();
        device2.setNotificationToken("invalid-token");
        device2.setId(2);

        handler.handle(Lists.newArrayList(result1, result2), Lists.newArrayList(device1, device2), new User(), true);

        // We should save the device 2 with the updated token
        verify(deviceRepository).save(Lists.newArrayList(device2));
    }
}
