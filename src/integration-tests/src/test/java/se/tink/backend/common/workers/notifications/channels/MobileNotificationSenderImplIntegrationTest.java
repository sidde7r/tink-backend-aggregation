package se.tink.backend.common.workers.notifications.channels;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.common.ServiceContext;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;


@Ignore
/**
 * TODO this is probably irrelevant
 */
public class MobileNotificationSenderImplIntegrationTest extends AbstractServiceIntegrationTest {

    private Provider<ServiceContext> contextProvider = () -> serviceContext;

    @Test
    public void testProxyGoogleNotification() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData();

        Device device = new Device();
        device.setAppId("se.tink.frontend.mobile");
        device.setNotificationToken(
                "APA91bGcbSFrAayNfAoPJam9BRdHvZYmizzpLJYXeeD-6lSH_LySiql1BzQDhfJVUI-P-Hfbeqw9why6PUzA7CU0ycackNktDk_WwBxT3_E12bWR6En7fLleAvyBSj5B4DrtZ6loAIi6");
        device.setType("android");

        serviceFactory.getDeviceService().updateDevice(authenticated(user), StringUtils.generateUUID(), device);

        Notification notification = new Notification("userId");
        notification.setDate(DateUtils.getToday());
        notification.setTitle("Test");
        notification.setMessage("This is a test");
        notification.setUrl("tink://open");

        List<Notification> notifications = Lists.newArrayList(notification);

        serviceContext.getConfiguration().getNotifications().getApplications().get("se.tink.frontend.mobile")
                .getGoogle().setProxyURL("http://localhost:8888/");
        MobileNotificationSender channel = new MobileNotificationSenderImpl(contextProvider,
                serviceContext.getConfiguration().getNotifications(),
                serviceContext.getRepository(DeviceRepository.class), injector.getInstance(MetricRegistry.class),
                Cluster.TINK,
                new DeepLinkBuilderFactory("tink://"));
        channel.start();
        try {
            channel.sendNotifications(user, notifications, false, false);
        } finally {
            channel.stop();
        }
    }

    @Test
    public void testProxyAppleNotification() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData();

        Device device = new Device();
        device.setAppId("se.tink.frontend.mobile");
        device.setNotificationToken("f79c76bb49a8cc5acf40718051de49ddc68aea3885d998ce6e30a006c2440157");
        device.setType("ios");

        serviceFactory.getDeviceService().updateDevice(authenticated(user), StringUtils.generateUUID(), device);

        SecureRandom random = new SecureRandom();

        Notification notification = new Notification("userId");
        notification.setDate(DateUtils.getToday());
        notification.setTitle("Test");
        notification.setMessage("This is a test " + random.nextInt(10000));
        notification.setUrl("tink://open");

        List<Notification> notifications = Collections.singletonList(notification);

        serviceContext.getConfiguration().getNotifications().getApplications().get("se.tink.frontend.mobile")
                .getApple().setProxyURL("http://localhost:8888/");
        MobileNotificationSender channel = new MobileNotificationSenderImpl(contextProvider,
                serviceContext.getConfiguration().getNotifications(),
                serviceContext.getRepository(DeviceRepository.class), injector.getInstance(MetricRegistry.class),
                Cluster.TINK,
                new DeepLinkBuilderFactory("tink://"));
        channel.start();
        try {
            channel.sendNotifications(user, notifications, false, false);
        } finally {
            channel.stop();
        }

        Thread.sleep(10000);
    }
}
