package se.tink.libraries.abnamro.utils;

import java.util.Date;
import java.util.Optional;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import se.tink.libraries.abnamro.config.AbnAmroAccountUpdatesConfiguration;
import se.tink.backend.core.AbnAmroSubscription;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

public class AbnAmroAccountsUpdateChecker {

    private final static LogUtils log = new LogUtils(AbnAmroAccountsUpdateChecker.class);

    public static boolean isEligibleForAccountUpdates(AbnAmroAccountUpdatesConfiguration configuration, User user,
            AbnAmroSubscription subscription, Optional<Date> lastLogin) {

        if (configuration == null) {
            log.warn(user.getId(), "No configuration available.");
            return false;
        }

        if (!configuration.isEnabled()) {
            log.debug(user.getId(), "Check for new account is disabled.");
            return false;
        }

        if (subscription == null) {
            log.debug(user.getId(), "Subscription is null.");
            return false;
        }

        if (!subscription.isActivated()) {
            log.debug(user.getId(), "Subscription is not active.");
            return false;
        }

        if (!lastLogin.isPresent()) {
            // No need to check for new accounts if the user hasn't logged in before (is register)
            log.debug(user.getId(), "User has not logged in before.");
            return false;
        }

        return hasValidStaleness(user, configuration, lastLogin.get());
    }

    /**
     * Allow for some staleness so that we don't check for new accounts too often.
     */
    private static boolean hasValidStaleness(User user, AbnAmroAccountUpdatesConfiguration configuration,
            Date lastLogin) {
        DateTime staleness = new DateTime(lastLogin).plusMinutes(configuration.getStalenessInMinutes());

        if (staleness.isAfterNow()) {

            Interval timeLeft = new Interval(DateTime.now(), staleness);

            log.info(user.getId(),
                    String.format("Not eligible for account updates (Waiting time = '%s')", timeLeft.toDuration()));
            return false;
        }

        log.info(user.getId(), String.format("Eligible for account updates (Time since last login = '%s')",
                new Interval(new DateTime(lastLogin), DateTime.now()).toDuration()));

        return true;
    }
}
