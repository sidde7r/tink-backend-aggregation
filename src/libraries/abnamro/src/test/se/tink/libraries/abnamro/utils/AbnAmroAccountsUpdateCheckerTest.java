package se.tink.libraries.abnamro.utils;

import java.util.Optional;
import java.util.Date;
import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.libraries.abnamro.config.AbnAmroAccountUpdatesConfiguration;
import se.tink.backend.core.AbnAmroSubscription;
import se.tink.backend.core.User;
import static org.assertj.core.api.Assertions.assertThat;

public class AbnAmroAccountsUpdateCheckerTest {

    @Test
    public void noConfigurationShouldNotBeEligible() {

        AbnAmroAccountUpdatesConfiguration configuration = null;
        User user = new User();
        AbnAmroSubscription subscription = new AbnAmroSubscription();
        Optional<Date> today = Optional.of(new Date());

        assertThat(AbnAmroAccountsUpdateChecker.isEligibleForAccountUpdates(configuration, user, subscription, today))
                .isFalse();
    }

    @Test
    public void disabledConfigurationShouldNotBeEligible() {

        AbnAmroAccountUpdatesConfiguration configuration = new AbnAmroAccountUpdatesConfiguration();
        configuration.setEnabled(false);

        User user = new User();
        AbnAmroSubscription subscription = new AbnAmroSubscription();
        Optional<Date> today = Optional.of(new Date());

        assertThat(AbnAmroAccountsUpdateChecker.isEligibleForAccountUpdates(configuration, user, subscription, today))
                .isFalse();
    }

    @Test
    public void emptySubscriptionShouldNotBeEligible() {

        AbnAmroAccountUpdatesConfiguration configuration = new AbnAmroAccountUpdatesConfiguration();
        configuration.setEnabled(true);

        User user = new User();
        AbnAmroSubscription subscription = null;
        Optional<Date> today = Optional.of(new Date());

        assertThat(AbnAmroAccountsUpdateChecker.isEligibleForAccountUpdates(configuration, user, subscription, today))
                .isFalse();
    }

    @Test
    public void inactivatedSubscriptionShouldNotBeEligible() {

        AbnAmroAccountUpdatesConfiguration configuration = new AbnAmroAccountUpdatesConfiguration();
        configuration.setEnabled(true);

        AbnAmroSubscription subscription = new AbnAmroSubscription();
        subscription.setActivationDate(null); // it is not activated

        User user = new User();
        Optional<Date> today = Optional.of(new Date());

        assertThat(AbnAmroAccountsUpdateChecker.isEligibleForAccountUpdates(configuration, user, subscription, today))
                .isFalse();
    }

    @Test
    public void noLastLoginDateShouldNotBeEligible() {

        AbnAmroAccountUpdatesConfiguration conf = new AbnAmroAccountUpdatesConfiguration();
        conf.setEnabled(true);

        AbnAmroSubscription subscription = new AbnAmroSubscription();
        subscription.setActivationDate(new Date());

        User user = new User();

        Optional<Date> noDate = Optional.empty();

        assertThat(AbnAmroAccountsUpdateChecker.isEligibleForAccountUpdates(conf, user, subscription, noDate))
                .isFalse();
    }

    /**
     * User logged in 9 minutes ago and we require a staleness of 10 minutes. Not eligible for a new check.
     */
    @Test
    public void nonStaleUserShouldNotBeEligible() {

        AbnAmroAccountUpdatesConfiguration conf = new AbnAmroAccountUpdatesConfiguration();
        conf.setEnabled(true);

        conf.setStalenessInMinutes(10);

        AbnAmroSubscription subscription = new AbnAmroSubscription();
        subscription.setActivationDate(new Date());

        User user = new User();

        Optional<Date> lastLogin = Optional.of(DateTime.now().minusMinutes(8).toDate());

        assertThat(AbnAmroAccountsUpdateChecker.isEligibleForAccountUpdates(conf, user, subscription, lastLogin))
                .isFalse();
    }

    /**
     * User logged in 20 minutes ago and we require a staleness of 10 minutes. Eligible for a new check.
     */
    @Test
    public void staleUserShouldBeEligible() {

        AbnAmroAccountUpdatesConfiguration conf = new AbnAmroAccountUpdatesConfiguration();
        conf.setEnabled(true);
        conf.setStalenessInMinutes(10);

        AbnAmroSubscription subscription = new AbnAmroSubscription();
        subscription.setActivationDate(new Date());

        Optional<Date> lastLogin = Optional.of(DateTime.now().minusMinutes(20).toDate());

        User user = new User();

        assertThat(AbnAmroAccountsUpdateChecker.isEligibleForAccountUpdates(conf, user, subscription, lastLogin))
                .isTrue();
    }

}
