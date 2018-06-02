package se.tink.backend.system.cronjob;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import rx.Observable;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.common.utils.NotificationUtils;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.system.rpc.SendNotificationsRequest;
import se.tink.libraries.identity.utils.IdentityTextUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;

/**
 * Class to handle logic for fraud service cron jobs.
 */
public class FraudCronJobs {

    private static final LogUtils log = new LogUtils(FraudCronJobs.class);

    private final SendNotificationsRequest sendNotificationsRequest;
    private final UserRepository userRepository;
    private final FraudDetailsRepository fraudDetailsRepository;
    private DeepLinkBuilderFactory deepLinkBuilderFactory;
    private final Cluster cluster;

    final Map<User, String> emailsByUser;

    @Inject
    public FraudCronJobs(UserRepository userRepository, FraudDetailsRepository fraudDetailsRepository,
            DeepLinkBuilderFactory deepLinkBuilderFactory, Cluster cluster) {
        this.userRepository = userRepository;
        this.fraudDetailsRepository = fraudDetailsRepository;
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
        this.cluster = cluster;

        emailsByUser = Maps.newHashMap();
        sendNotificationsRequest = new SendNotificationsRequest();
    }

    public SendNotificationsRequest getSendNotificationsRequest() {
        return sendNotificationsRequest;
    }

    public Map<User, String> getEmailsByUserMap() {
        return emailsByUser;
    }

    public void createFraudReminders() {
        // Get all users with fraud turned on.

        Observable<User> fraudUsers = userRepository.streamAll().filter(
                u -> !Strings.isNullOrEmpty(u.getProfile().getFraudPersonNumber()));

        final Date now = new Date();

        fraudUsers.forEach(user -> {

            // Check for unhandled fraud details created more than 12 h ago.

            Iterable<FraudDetails> unhandledFraudDetails = Iterables.filter(
                    fraudDetailsRepository.findAllByUserId(user.getId()), fd -> {
                        boolean unhandled = fd.getStatus() == FraudStatus.CRITICAL;
                        double hoursBetween = DateUtils.getNumberOfHoursBetween(fd.getCreated(), now);
                        return unhandled && hoursBetween >= 12;
                    });

            if (Iterables.size(unhandledFraudDetails) > 0) {

                // Find youngest details and send push or email according to hours between now and created time.

                FraudDetails youngestFraudDetails = FraudUtils.CREATED_ORDERING.max(unhandledFraudDetails);
                int hoursSinceCreated = (int) DateUtils.getNumberOfHoursBetween(youngestFraudDetails.getCreated(), now);

                boolean encrypted = NotificationUtils.shouldSendEncrypted(cluster);

                Optional<Notification> notificationOptional = Optional.empty();

                switch (hoursSinceCreated) {
                case 12:
                    emailsByUser.put(
                            user,
                            createReminderMessage(user, youngestFraudDetails, Iterables.size(unhandledFraudDetails)));
                    break;
                case 24:
                    notificationOptional = createFraudPushReminder(user, youngestFraudDetails,
                            Iterables.size(unhandledFraudDetails));

                    notificationOptional.ifPresent(notification -> sendNotificationsRequest
                            .addUserNotification(user, notification, encrypted));
                    break;

                // Only for TEST_FRAUD_REMINDERS_ON users.

                case 36:
                case 72:
                case 96:
                case 144:
                case 192:
                    if (user.getFlags().contains(FeatureFlags.TEST_FRAUD_REMINDERS_ON)) {
                        emailsByUser.put(
                                user,
                                createReminderMessage(user, youngestFraudDetails,
                                        Iterables.size(unhandledFraudDetails)));
                    }
                    break;
                case 48:
                    if (user.getFlags().contains(FeatureFlags.TEST_FRAUD_REMINDERS_ON)) {
                        notificationOptional = createFraudPushReminder(user, youngestFraudDetails,
                                Iterables.size(unhandledFraudDetails));

                        notificationOptional.ifPresent(notification -> sendNotificationsRequest
                                .addUserNotification(user, notification, encrypted));
                    }
                    break;
                }
            }
        });
    }

    private String createReminderMessage(User user, FraudDetails fraudDetails, int size) {
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
        String source = IdentityTextUtils.getSourceFromType(fraudDetails.getType());
        return Catalog.format(catalog.getPluralString("{0} new event from {1}", "{0} new events from i.a. {1}", size),
                size, source);
    }

    private Optional<Notification> createFraudPushReminder(User user, FraudDetails fraudDetails, int nbrOfUnhandled) {
        Date now = new Date();
        String key = FraudUtils.generateNotificationKey("fraud-reminder", fraudDetails.getCreated());
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        Notification.Builder builder = new Notification.Builder()
                .userId(user.getId())
                .key(key)
                .date(fraudDetails.getCreated())
                .generated(now)
                .type("fraud.reminder.push")
                .title(catalog.getString("ID Control"))
                .message(catalog.getPluralString(
                        "You have a new event that needs your attention.",
                        "You have new events that need your attention.", nbrOfUnhandled))
                .url(deepLinkBuilderFactory.fraudReminder().build())
                .groupable(false);

        try {
            return Optional.of(builder.build());
        } catch (IllegalArgumentException e) {
            log.error(user.getId(), "Could not generate notification", e);
            return Optional.empty();
        }
    }
}
