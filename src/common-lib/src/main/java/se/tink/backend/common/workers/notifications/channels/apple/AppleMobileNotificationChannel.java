package se.tink.backend.common.workers.notifications.channels.apple;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.proxy.HttpProxyHandlerFactory;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import io.dropwizard.lifecycle.Managed;
import io.netty.util.concurrent.Future;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import se.tink.backend.common.config.AppleKeystoreConfiguration;
import se.tink.backend.common.config.AppleNotificationConfiguration;
import se.tink.backend.common.retry.RetryerBuilder;
import se.tink.backend.common.utils.AbnAmroUserAgentUtils;
import se.tink.backend.common.utils.NotificationUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.common.workers.notifications.channels.MobileNotificationChannel;
import se.tink.backend.common.workers.notifications.channels.encryption.EncryptedPushNotificationContainer;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Predicates;

public class AppleMobileNotificationChannel implements MobileNotificationChannel, Managed {

    private static final LogUtils log = new LogUtils(AppleMobileNotificationChannel.class);
    private final static Retryer<Boolean> PUSH_NOTIFICATION_RETRYER = RetryerBuilder
            .<Boolean>newBuilder(log, "submitting notification to Apple")
            .withStopStrategy(StopStrategies.stopAfterDelay(10, TimeUnit.MINUTES))
            .withWaitStrategy(WaitStrategies.fibonacciWait())
            .build();

    private final Cluster cluster;
    private final String topic;
    private final boolean isSandbox;
    private final boolean isUpdateAppIconNotificationBadge;
    private final NotificationResponseHandler successHandler;
    private final NotificationResponseHandler failureHandler;
    private final TransportErrorHandler transportErrorHanlder;
    private final String appId;

    private ApnsClient apnsClient;

    public AppleMobileNotificationChannel(String appId, AppleNotificationConfiguration config,
            Cluster cluster,
            NotificationResponseHandler successHandler,
            NotificationResponseHandler failureHandler, TransportErrorHandler transportErrorHandler) {

        this.appId = appId;
        this.cluster = cluster;
        this.topic = config.getTopic();
        this.isSandbox = config.isSandbox();
        this.isUpdateAppIconNotificationBadge = config.isUpdateAppIconNotificationBadge();
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.transportErrorHanlder = transportErrorHandler;

        final AppleKeystoreConfiguration keystore = config.getKeystore();
        try {
            File certificate = new File(String.format("data/push/%s", keystore.getFilename()));
            ApnsClientBuilder builder = new ApnsClientBuilder()
                    .setClientCredentials(certificate, keystore.getPassword())
                    .setConnectionTimeout(30, TimeUnit.SECONDS);

            if (config.getProxyURL().isPresent()) {
                URL proxy = config.getProxyURL().get();
                log.info("Using proxy for push notifications: " + proxy + ", " + topic);
                builder.setProxyHandlerFactory(
                        new HttpProxyHandlerFactory(new InetSocketAddress(proxy.getHost(), proxy.getPort())));
            }

            final String server;
            if (isSandbox) {
                server = ApnsClientBuilder.DEVELOPMENT_APNS_HOST;
            } else {
                server = ApnsClientBuilder.PRODUCTION_APNS_HOST;
            }

            apnsClient = builder.setApnsServer(server).build();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not instantiate Apple APNS push notification service client. Keystore: " +
                            keystore.getFilename(), e);
        }
    }

    @Override
    public boolean send(Notification notification, List<Device> devices, User user, boolean encrypted,
            int unreadNotifications) {

        boolean sent = false;
        for (Device device : devices) {
            sent |= send(notification, device, user, unreadNotifications, encrypted);
        }

        return sent;
    }

    private boolean send(final Notification notification, final Device device, final User user, int unreadNotifications,
            final boolean encrypted) {

        try {
            final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();

            // Send encrypted
            if (encrypted) {
                EncryptedPushNotificationContainer encryptedNotification = NotificationUtils
                        .getEncryptedPushNotification(notification, device);

                if (encryptedNotification == null) {
                    log.warn(user.getId(), String.format("Device:%d. EncryptedNotification is null.", device.getId()));
                } else {

                    if (Strings.isNullOrEmpty(encryptedNotification.getKey())) {
                        log.warn(user.getId(),
                                String.format("Device:%d. Encryption key is null or empty.", device.getId()));
                    }

                    if (Strings.isNullOrEmpty(encryptedNotification.getPayload())) {
                        log.warn(user.getId(),
                                String.format("Device:%d. Encryption payload is null or empty.", device.getId()));
                    }

                }

                String serializedEncryptedNotification = SerializationUtils.serializeToString(encryptedNotification);
                payloadBuilder.addCustomProperty("encrypted-notification", serializedEncryptedNotification);

                // Set the title and the message if the device (userAgent) "always" can decrypt encrypted notifications.
                // These two fields needs to be set for the Notification Service Extension in the app to receive the
                // notification or else it will be considered as a silent notification that can't be decrypted if the
                // app isn't running.
                if (Objects.equals(Cluster.ABNAMRO, cluster) && AbnAmroUserAgentUtils
                        .canAlwaysDecryptNotifications(device.getUserAgent())) {
                    payloadBuilder.setAlertTitle(notification.getTitle());
                    payloadBuilder.setAlertBody(notification.getMessage());
                }
            } else {
                if (!Strings.isNullOrEmpty(notification.getTitle())
                        && !Strings.isNullOrEmpty(notification.getMessage())) {
                    payloadBuilder.setAlertTitle(notification.getTitle().trim());
                    payloadBuilder.setAlertBody(notification.getMessage().trim());
                } else if (!Strings.isNullOrEmpty(notification.getTitle())) {
                    payloadBuilder.setAlertTitle(notification.getTitle().trim());
                } else if (!Strings.isNullOrEmpty(notification.getMessage())) {
                    payloadBuilder.setAlertBody(notification.getMessage().trim());
                } else {
                    log.warn(user.getId(),
                            String.format("Not sending notification without body nor title: %s", notification));
                    return false;
                }

                payloadBuilder.setSoundFileName("default");

                if (!Strings.isNullOrEmpty(notification.getUrl())) {
                    payloadBuilder.addCustomProperty("url", notification.getUrl());
                }

                if (!Strings.isNullOrEmpty(notification.getKey())) {
                    payloadBuilder.addCustomProperty("key", notification.getKey());
                }

                payloadBuilder.addCustomProperty("type", notification.getType());
            }

            if (isUpdateAppIconNotificationBadge) {
                payloadBuilder.setBadgeNumber(unreadNotifications);
            }
            payloadBuilder.setContentAvailable(true);

            // This makes it possible to modify the content before displaying it. Needed for encrypted push
            // notifications if the decryption is done in the Notification Service Extension.
            payloadBuilder.setMutableContent(true);

            String deviceToken = device.getNotificationToken();
            String payLoad = payloadBuilder.buildWithDefaultMaximumLength();

            // Collapse identifier (apns-collapse-id) for the notification which will allow it to supersede or be
            // superseded by other notifications with the same collapse identifier.
            String collapseId = notification.getId();

            // The time at which Apple's servers should stop trying to deliver this message. Not using an invalidation
            // time means that Apple only will try to deliver the notification once, this causes issues if users don't
            // have a network connection.
            Date invalidationTime = new DateTime().plusWeeks(1).toDate();

            SimpleApnsPushNotification toBePushed = new SimpleApnsPushNotification(deviceToken, topic, payLoad,
                    invalidationTime, DeliveryPriority.IMMEDIATE, collapseId);

            final Future<PushNotificationResponse<SimpleApnsPushNotification>> result = apnsClient
                    .sendNotification(toBePushed);
            return PUSH_NOTIFICATION_RETRYER.call(() -> {

                // result.get() will throw ClientNotConnectedException if not connected. See
                // https://github.com/relayrides/pushy#sending-push-notifications.

                PushNotificationResponse<SimpleApnsPushNotification> response = result.get();

                if (response.isAccepted()) {

                    if (successHandler != null) {
                        successHandler.handle(notification, device, encrypted, response);
                    }

                    return true;
                } else {

                    if (failureHandler != null) {
                        failureHandler.handle(notification, device, encrypted, response);
                    }

                    return false;
                }
            });

        } catch (Exception e) {
            transportErrorHanlder.handle(notification, device, encrypted);
            log.error(user.getId(), "Could not send iOS notification", e);
        }
        return false;
    }

    @Override
    public Predicate<Device> getPredicate() {
        return Predicates.IOS_DEVICE;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {

        if (!apnsClient.close().await(30, TimeUnit.SECONDS)) {
            throw new RuntimeException("Could not close Apple push notification service connection in 30 seconds.");
        }
    }

}
