package se.tink.backend.common.workers.notifications.channels;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.dropwizard.lifecycle.Managed;
import java.security.Security;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.AppleNotificationConfiguration;
import se.tink.backend.common.config.GoogleNotificationConfiguration;
import se.tink.backend.common.config.NotificationsApplicationConfiguration;
import se.tink.backend.common.config.NotificationsConfiguration;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.notifications.channels.apple.AppleMobileNotificationChannel;
import se.tink.backend.common.workers.notifications.channels.apple.FailedNotificationHandler;
import se.tink.backend.common.workers.notifications.channels.apple.SuccessNotificationHandler;
import se.tink.backend.common.workers.notifications.channels.apple.TransportErrorHandler;
import se.tink.backend.common.workers.notifications.channels.google.GoogleMobileNotificationChannel;
import se.tink.backend.common.workers.notifications.channels.operators.MobileNotificationDelegator;
import se.tink.backend.common.workers.notifications.channels.operators.MobileNotificationOperator;
import se.tink.backend.common.workers.notifications.channels.operators.MobileNotificationRateLimiter;
import se.tink.backend.common.workers.notifications.channels.operators.MobileNotificationSuccessfulInstrumenter;
import se.tink.backend.common.workers.notifications.channels.operators.MobileNotificationTracker;
import se.tink.backend.core.Device;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.abnamro.utils.AbnAmroLegacyUserUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class MobileNotificationSenderImpl implements MobileNotificationSender {
    private static final LogUtils log = new LogUtils(MobileNotificationSenderImpl.class);
    private static final int MAX_NBR_OF_NOTIFICATIONS = 2;
    private static final MetricId NOTIFICATIONS = MetricId.newId("notifications");

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private final DeviceRepository deviceRepository;
    private final MetricRegistry metricRegistry;
    private final int maxAgeOfNotifications;
    private final NotificationsConfiguration notificationsConfiguration;
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;
    private final ImmutableList<MobileNotificationChannel> channels;
    private final Provider<ServiceContext> contextProvider;
    private final Cluster cluster;

    /**
     * Subservices that should be managed through {@link #start()} and {@link #stop()}.
     */
    private final ImmutableList<Managed> subManaged;

    @Inject
    public MobileNotificationSenderImpl(Provider<ServiceContext> serviceContextProvider,
            NotificationsConfiguration notificationsConfiguration, DeviceRepository deviceRepository,
            MetricRegistry metricRegistry, Cluster cluster, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        this.contextProvider = serviceContextProvider;
        this.deviceRepository = deviceRepository;
        this.metricRegistry = metricRegistry;

        maxAgeOfNotifications = notificationsConfiguration.getMaxAgeDays();
        this.notificationsConfiguration = notificationsConfiguration;
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
        this.cluster = cluster;

        // TODO: Possibly inject these in the future.
        ImmutableList.Builder<Managed> subManagedBuilder = ImmutableList
                .builder();
        if (notificationsConfiguration.isEnabled()) {
            ImmutableList.Builder<MobileNotificationChannel> channelsBuilder = ImmutableList.builder();
            for (Entry<String, NotificationsApplicationConfiguration> entry : notificationsConfiguration.getApplications()
                    .entrySet()) {
                String appId = entry.getKey();
                NotificationsApplicationConfiguration appIdConfiguration = entry.getValue();
                
                AppIdPredicate appIdPredicate = new AppIdPredicate(appId);

                AppleNotificationConfiguration appleConfig = appIdConfiguration.getApple();
                if (appleConfig != null) {

                    AppleMobileNotificationChannel appleChannel = new AppleMobileNotificationChannel(
                            appId, appleConfig, cluster, new SuccessNotificationHandler(metricRegistry),
                            new FailedNotificationHandler(deviceRepository, metricRegistry),
                            new TransportErrorHandler(metricRegistry));

                    subManagedBuilder.add(appleChannel);
                    channelsBuilder.add(new DeviceFilteredChannel(appleChannel, appIdPredicate));
                }

                GoogleNotificationConfiguration googleConfig = appIdConfiguration.getGoogle();
                if (googleConfig != null) {
                    GoogleMobileNotificationChannel googleChannel = new GoogleMobileNotificationChannel(
                            deviceRepository, googleConfig, metricRegistry, notificationsConfiguration);
                    channelsBuilder.add(new DeviceFilteredChannel(googleChannel, appIdPredicate));
                }
            }
            channels = channelsBuilder.build();
        } else {
            channels = ImmutableList.<MobileNotificationChannel> of(new LoggingMobileNotificationChannel());
        }
        
        log.info(String.format("Registered %s channels, channels: ", channels.size()));
        subManaged = subManagedBuilder.build();
    }

    @Override
    public void sendNotifications(User user, List<Notification> notifications, boolean shouldBeTracked,
            boolean encrypted) {

        if (notifications.isEmpty()) {
            return;
        }

        List<Device> devices = deviceRepository.findByUserId(user.getId());

        if (devices == null || devices.isEmpty()) {
            return;
        }

        List<Notification> currentNotifications = filterNotifications(notifications);
        // Badge count, i.e. not necessarily the actual number of notifications.
        int unreadNotifications = currentNotifications.size();
        if (notificationsConfiguration.shouldGroupNotifications()) {
            currentNotifications = groupNotifications(currentNotifications, user);
        }


        // Legacy Grip uses the tink:// prefix, modern Grip uses grip://. If a user hasn't migrated to
        // the new user model their user should receive notifications with Tink in it instead of Grip.
        // TODO Remove conditional when Grip <3.0 are deprecated
        if (Objects.equals(cluster, Cluster.ABNAMRO)) {
            AbnAmroLegacyUserUtils.replaceGripPrefixForLegacyUsers(user.getUsername(), currentNotifications);
        }

        // Send filtered notifications
        MobileNotificationOperator notificationChain = getNotificationChain(
                contextProvider.get(), channels, shouldBeTracked);

        log.info(user.getId(), String.format(
                "Sending mobile notifications (Input = '%d', Filtered = '%d', Grouped/Sent = '%d', Badge Count = '%d')",
                notifications.size(), unreadNotifications, currentNotifications.size(), unreadNotifications));

        for (Notification notification : currentNotifications) {
            notificationChain.process(notification, devices, user, encrypted, unreadNotifications);
            metricRegistry.meter(NOTIFICATIONS.label("encrypted", encrypted)).inc();
        }
    }

    private List<Notification> filterNotifications(Iterable<Notification> notifications) {
        // Filter old notifications freshness (max maxAgeOfNotifications days old).

        final Date today = DateUtils.getToday();

        return FluentIterable.from(notifications).filter(
                n -> (DateUtils.getNumberOfDaysBetween(n.getDate(), today) <= maxAgeOfNotifications)).toList();
    }

    private List<Notification> groupNotifications(Iterable<Notification> notifications, User user) {
        List<Notification> groupedNotifications = Lists.newArrayList(notifications);
        final int unreadNotifications = Iterables.size(notifications);
        final Date today = DateUtils.getToday();
        final String locale = user.getProfile().getLocale();

        // See if there are any notifications that should be grouped
        if (unreadNotifications > MAX_NBR_OF_NOTIFICATIONS) {

            final List<Notification> nonGroupableNotifications = FluentIterable.from(notifications)
                    .filter(n -> !n.isGroupable()).toList();

            // At least two of the notifications are groupable, so let's group them.
            if (nonGroupableNotifications.size() < (unreadNotifications - 1)) {

                groupedNotifications = Lists.newArrayList(nonGroupableNotifications);

                final Catalog catalog = Catalog.getCatalog(locale);

                final Notification.Builder groupedNotification = new Notification.Builder()
                        .userId(user.getId())
                        .date(today)
                        .type("grouped-notifications")
                        .key("grouped-notifications")
                        .url(deepLinkBuilderFactory.groupedEvents().build())
                        .title(catalog.getString("New events"))
                        .message(catalog.getString("You have new events in your feed"));

                try {
                    groupedNotifications.add(groupedNotification.build());
                } catch (IllegalArgumentException e) {
                    log.error(user.getId(), "Could not generate notification", e);
                }
            }
        }

        return groupedNotifications;
    }

    MobileNotificationOperator getNotificationChain(ServiceContext context,
            List<MobileNotificationChannel> channels, boolean shouldBeTracked) {
        MobileNotificationOperator successfulInstrumenter = new MobileNotificationSuccessfulInstrumenter(
                metricRegistry);
        MobileNotificationOperator delegator = new MobileNotificationDelegator(successfulInstrumenter, channels);
        MobileNotificationOperator rateLimiter;

        if (shouldBeTracked) {
            MobileNotificationOperator tracker = new MobileNotificationTracker(delegator, context);
            rateLimiter = new MobileNotificationRateLimiter(tracker);
        } else {
            rateLimiter = new MobileNotificationRateLimiter(delegator);
        }

        return rateLimiter;
    }

    @Override
    public void start() throws Exception {
        for (Managed subService : subManaged) {
            subService.start();
        }
    }

    @Override
    public void stop() throws Exception {
        for (Managed subService : subManaged) {
            subService.stop();
        }
    }
}
