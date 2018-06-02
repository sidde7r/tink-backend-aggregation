package se.tink.backend.common.mail.monthly.summary.calculators;

import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import se.tink.backend.common.mail.monthly.summary.model.ActivityData;
import se.tink.backend.core.Activity;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Transaction;

import java.util.List;

import static se.tink.backend.common.mail.monthly.summary.utils.Filters.withPeriod;

public class ActivityDataCalculator {

    private Iterable<Activity> activities;

    public ActivityDataCalculator(Iterable<Activity> activities) {
        this.activities = activities;
    }

    @SuppressWarnings("unchecked")
    public ActivityData getActivityData(String period, ResolutionTypes resolution, int periodBreakDate) {
        int lowBalanceCount = 0;
        int largeExpenseCount = 0;
        int moreThanUsualCount = 0;

        ImmutableList<Activity> filteredActivities = FluentIterable
                .from(activities)
                .filter(withPeriod(period, resolution, periodBreakDate))
                .toList();

        for (Activity activity : filteredActivities) {
            if (Objects.equal(activity.getType(), "large-expense")) {
                largeExpenseCount++;
            } else if (Objects.equal(activity.getType(), "large-expense/multiple")) {
                largeExpenseCount += ((List<Transaction>) activity.getContent()).size();
            } else if (Objects.equal(activity.getType(), "balance")) {
                lowBalanceCount++;
            } else if (Objects.equal(activity.getType(), "unusual-category")) {
                moreThanUsualCount++;
            }
        }

        ActivityData events = new ActivityData();
        events.setLowBalanceCount(lowBalanceCount);
        events.setLargeExpenseCount(largeExpenseCount);
        events.setMoreThanUsualCount(moreThanUsualCount);

        return events;
    }
}
