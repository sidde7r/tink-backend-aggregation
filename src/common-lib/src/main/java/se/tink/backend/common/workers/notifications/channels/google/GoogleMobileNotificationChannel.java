package se.tink.backend.common.workers.notifications.channels.google;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.api.client.http.HttpStatusCodes;
import java.util.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import se.tink.backend.common.config.GoogleNotificationConfiguration;
import se.tink.backend.common.config.NotificationsConfiguration;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.common.retry.RetryerBuilder;
import se.tink.backend.common.workers.notifications.channels.MobileNotificationChannel;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Predicates;

public class GoogleMobileNotificationChannel implements MobileNotificationChannel {

    private static final LogUtils log = new LogUtils(GoogleMobileNotificationChannel.class);
    private static final String GCM_URL = "https://android.googleapis.com/gcm/send";
    private final static Retryer<GooglePushNotificationResponse> PUSH_NOTIFICATION_RETRYER = RetryerBuilder
            .<GooglePushNotificationResponse>newBuilder(log, "submitting notification to Google")
            .withStopStrategy(StopStrategies.stopAfterDelay(5, TimeUnit.MINUTES))
            .withWaitStrategy(WaitStrategies.fibonacciWait())
            .retryIfException()
            .build();

    private static final MetricId NOTIFICATIONS = MetricId.newId("mobile_notifications").label("service", "google");
    private static final MetricId NOTIFICATIONS_SUCCESS = NOTIFICATIONS.label("success", true);
    private static final MetricId NOTIFICATIONS_FAILED = NOTIFICATIONS.label("success", false);

    private String gcmKey;
    private MetricRegistry metricRegistry;
    private Client gcmClient;
    private Optional<URL> proxyURL;
    private final GoogleExpiredPushNotificationHandler expiredPushNotificationHandler;
    private NotificationsConfiguration notificationsConfiguration;

    public GoogleMobileNotificationChannel(DeviceRepository deviceRepository,
            GoogleNotificationConfiguration config, MetricRegistry metricRegistry,
            NotificationsConfiguration notificationsConfiguration) {

        this.proxyURL = config.getProxyURL();
        this.gcmKey = config.getApiKey();
        this.notificationsConfiguration = notificationsConfiguration;
        this.gcmClient = createGCMClient();
        this.expiredPushNotificationHandler = new GoogleExpiredPushNotificationHandler(deviceRepository,
                metricRegistry);
        this.metricRegistry = metricRegistry;
    }

    private Client createGCMClient() {
        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();

        if (proxyURL.isPresent()) {
            log.info("Using proxy for push notifications: " + proxyURL.get());
            clientConfig.getProperties().put(ApacheHttpClient4Config.PROPERTY_PROXY_URI, proxyURL.get().toString());
        }

        // See http://stackoverflow.com/questions/27167588/multi-threaded-connection-manager-with-jersey-apache-client-4
        ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager();

        clientConfig.getProperties().put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager);

        return ApacheHttpClient4.create(clientConfig);
    }

    private boolean send(Notification notification, List<Device> devices, User user, boolean encrypted) {

        boolean wasSuccessful = false;
        List<GooglePushNotificationResponseResult> results = Lists.newArrayList();
        List<Device> usedDevices = Lists.newArrayList();

        try {

            List<GooglePushNotificationRequest> requests = GooglePushNotificationRequest.builder()
                    .withNotification(notification)
                    .withDevices(devices)
                    .withEncryption(encrypted)
                    .withFallbackUrl(notificationsConfiguration.getDeepLinkPrefix() + "open")
                    .build();

            for (GooglePushNotificationRequest request : requests) {
                GooglePushNotificationResponse response = sendRequest(request);

                log.debug(user.getId(), response.toString());

                if (response.getSuccess() > 0) {
                    metricRegistry.meter(NOTIFICATIONS_SUCCESS.label("encrypted", encrypted))
                            .inc(response.getSuccess());
                    wasSuccessful = true;
                }

                if (response.getFailure() > 0) {
                    metricRegistry.meter(NOTIFICATIONS_FAILED.label("encrypted", encrypted)).inc(response.getFailure());
                }

                log.info(user.getId(), String.format(
                        "Push notifications sent(Key = '%s', Success = '%d', Failed = '%d', Encrypted = '%s')",
                        notification.getKey(), response.getSuccess(), response.getFailure(), encrypted));

                // Keep track of the results of this notification
                results.addAll(response.getResults());

                // Keep track of the devices that we send this notification to
                usedDevices.addAll(request.getDevices());
            }

        } catch (Exception e) {
            log.error(user.getId(), "Could not send Android notification", e);
        }

        // Process the expired tokens with the devices that where used
        expiredPushNotificationHandler.handle(results, usedDevices, user, encrypted);

        return wasSuccessful;
    }

    private GooglePushNotificationResponse sendRequest(final GooglePushNotificationRequest request) throws Exception {

        Callable<GooglePushNotificationResponse> callable = () -> {
            ClientResponse clientResponse = gcmClient.resource(GCM_URL)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", "key=" + gcmKey)
                    .post(ClientResponse.class, request);

            if (clientResponse.getStatus() == HttpStatusCodes.STATUS_CODE_OK) {
                return clientResponse.getEntity(GooglePushNotificationResponse.class);
            } else {

                String message = clientResponse.getEntity(String.class);

                log.warn(String.format("Submitting notification to google failed. Retrying... (Message = '%s')",
                        message));

                throw new RuntimeException(message);
            }
        };

        return PUSH_NOTIFICATION_RETRYER.call(callable);
    }

    @Override
    public boolean send(Notification notification, List<Device> devices, User user, boolean encrypted,
            int unreadNotifications) {

        // Deliberately ignoring `unreadNotifications` here.
        return send(notification, devices, user, encrypted);

    }

    @Override
    public Predicate<Device> getPredicate() {
        return Predicates.ANDROID_DEVICE;
    }

}
