package se.tink.backend.common.mail.monthly.summary.utils;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import java.util.Date;
import rx.functions.Func1;
import se.tink.backend.common.mail.SubscriptionHelper;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.SubscriptionType;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;

public class Filters {

    public static Func1<User, Boolean> userWithMonthlyBreakDayOnDate(final Date date) {
        return user -> new PeriodUtils(user.getProfile()).isFirstDayInCurrentPeriod(date);
    }

    public static Func1<User, Boolean> isTinkUser() {
        return user -> !user.getFlags().contains(FeatureFlags.NO_TINK_USER);
    }

    public static Func1<User, Boolean> isNotAnonymous() {
        return user -> !user.getFlags().contains(FeatureFlags.ANONYMOUS);
    }

    public static Func1<User, Boolean> usersWithUserIdAbove(final String userId) {
        return user -> user.getId().compareTo(userId) > 0;
    }

    public static Func1<User, Boolean> userSubscribesToMonthlyEmails(final SubscriptionHelper sh) {
        return user -> sh.subscribesTo(user.getId(), SubscriptionType.MONTHLY_SUMMARY_EMAIL);
    }

    public static Predicate<Transaction> transactionIsWithinPeriod(final String period,
            final UserProfile profile) {
        return t -> (Objects.equal(period,
                DateUtils.getMonthPeriod(t.getDate(),
                        profile.getPeriodMode(), profile.getPeriodAdjustedDay())));
    }

    public static Predicate<Statistic> withPeriod(final String period) {
        return s -> Objects.equal(s.getPeriod(), period);
    }

    public static Predicate<Statistic> withResolution(final ResolutionTypes resolution) {
        return s -> s.getResolution() == resolution;
    }

    public static Predicate<Statistic> ofType(final String type) {
        return s -> Objects.equal(s.getType(), type);

    }

    public static Predicate<Activity> withPeriod(final String period,
            final ResolutionTypes resolution, final int periodBreakDate) {
        return a -> (Objects.equal(period,
                DateUtils.getMonthPeriod(a.getDate(), resolution, periodBreakDate)));

    }

}
