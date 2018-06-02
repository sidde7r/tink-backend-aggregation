package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.ImmutableMap;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Currency;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import se.tink.libraries.date.ResolutionTypes;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static se.tink.backend.common.workers.activity.generators.UnusualActivityGenerator.RECURRING_EXPENSES_METRIC;

@RunWith(JUnitParamsRunner.class)
public class UnusualActivityGeneratorTest {

    ActivityGeneratorContext context = new ActivityGeneratorContext();
    MetricRegistry metricRegistry = new MetricRegistry();

    @Before public void setUp() {
        UserProfile profile = new UserProfile();
        profile.setCurrency("currencyCode");
        profile.setPeriodMode(ResolutionTypes.MONTHLY);
        User user = new User();
        user.setProfile(profile);
        context.setUser(user);

        context.setCurrencies(ImmutableMap.of("currencyCode", new Currency()));
        context.setCatalog(Catalog.getCatalog("sv_SE"));

        Category category = new Category();
        category.setId("categoryId");
        category.setType(CategoryTypes.EXPENSES);
        context.setCategoriesByCodeForLocale(ImmutableMap.of("categoryId", category));
        context.setCategoryConfiguration(new SECategories());
        int statisticValue = 21;
        int months = 6;
        context.setStatistics(new ArrayList<>(createCountByCategoryStatistics(statisticValue, months).values()));
    }

    @Test public void generateActivityWhenNoStatistics() {
        context.setStatistics(Collections.<Statistic>emptyList());

        new UnusualActivityGenerator(metricRegistry, new DeepLinkBuilderFactory("")).generateActivity(context);

        assertTrue(context.getActivities().isEmpty());
    }

    @Test public void generateActivityWhenNoTransactions() {
        context.setStatistics(Collections.<Statistic>emptyList());

        new UnusualActivityGenerator(metricRegistry, new DeepLinkBuilderFactory("")).generateActivity(context);

        assertTrue(context.getActivities().isEmpty());
    }

    @Test public void generateHighActivity() {
        Statistic juneStatistic = createTemplateStatistic(Statistic.Types.EXPENSES_BY_CATEGORY);
        juneStatistic.setPeriod("2016-06");
        juneStatistic.setValue(-20);
        Statistic decemberStatistic = createTemplateStatistic(Statistic.Types.EXPENSES_BY_CATEGORY);
        decemberStatistic.setPeriod("2016-12");
        decemberStatistic.setValue(-50);
        context.getStatistics().addAll(asList(juneStatistic, decemberStatistic));

        int minimumTransactionsInCategory = 1;
        int maximumNoSpendingMonths = 9;
        new UnusualActivityGenerator(minimumTransactionsInCategory, maximumNoSpendingMonths, metricRegistry,
                new DeepLinkBuilderFactory("")).generateActivity(context);

        assertEquals(1, context.getActivities().size());
        assertEquals(Activity.Types.UNUSUAL_CATEGORY_HIGH, context.getActivities().get(0).getType());
        assertEquals(0,
                metricRegistry.meter(RECURRING_EXPENSES_METRIC.label("detected", "yes")).getCount());
        assertEquals(1,
                metricRegistry.meter(RECURRING_EXPENSES_METRIC.label("detected", "no")).getCount());
    }

    @Test public void generateActivityWhenTooManyNoSpendingMonths() {
        Statistic juneStatistic = createTemplateStatistic(Statistic.Types.EXPENSES_BY_CATEGORY);
        juneStatistic.setPeriod("2016-06");
        juneStatistic.setValue(-20);
        Statistic decemberStatistic = createTemplateStatistic(Statistic.Types.EXPENSES_BY_CATEGORY);
        decemberStatistic.setPeriod("2016-12");
        decemberStatistic.setValue(-50);
        context.getStatistics().addAll(asList(juneStatistic, decemberStatistic));

        int minimumTransactionsInCategory = 1;
        int maximumNoSpendingMonths = 2;
        new UnusualActivityGenerator(minimumTransactionsInCategory, maximumNoSpendingMonths, metricRegistry,
                new DeepLinkBuilderFactory("")).generateActivity(context);

        assertTrue(context.getActivities().isEmpty());
        assertEquals(0,
                metricRegistry.meter(RECURRING_EXPENSES_METRIC.label("detected", "yes")).getCount());
        assertEquals(0,
                metricRegistry.meter(RECURRING_EXPENSES_METRIC.label("detected", "no")).getCount());
    }

    @Test public void generateLowActivityWithTwoNoSpendingMonthsForZeroValue() {
        int numberOfMonths = 9;
        int monthlySpending = -100;
        Map<String, Statistic> statistics = createExpensesByCategory(monthlySpending, numberOfMonths);

        // make two (maximumNoSpendingMonths) months empty
        statistics.get("2016-02").setValue(0);
        statistics.get("2016-03").setValue(0);

        // we should get low spending activity for this month
        // zero value of current month should not trigger no spending months threshold
        statistics.get("2016-09").setValue(0);
        context.getStatistics().addAll(new ArrayList<>(statistics.values()));

        int minimumTransactionsInCategory = 1;
        int maximumNoSpendingMonths = 2;
        new UnusualActivityGenerator(minimumTransactionsInCategory, maximumNoSpendingMonths, metricRegistry,
                new DeepLinkBuilderFactory("")).generateActivity(context);

        assertEquals(1, context.getActivities().size());
        Activity activity = context.getActivities().get(0);
        assertEquals(Activity.Types.UNUSUAL_CATEGORY_LOW, activity.getType());
        assertEquals(9, new DateTime(activity.getDate()).monthOfYear().get());
    }

    @Test public void generateNoActivityForPeriodicExpense() {
        int numberOfMonths = 9;
        int monthlySpending = -100;
        Map<String, Statistic> statistics = createExpensesByCategory(monthlySpending, numberOfMonths);

        // large expenses
        statistics.get("2016-05").setValue(-400);   // no activity, too few rolling data points
        statistics.get("2016-07").setValue(-400);   // high expense activity
        statistics.get("2016-09").setValue(-400);   // no activity, pattern detected

        context.getStatistics().addAll(new ArrayList<>(statistics.values()));
        int minimumTransactionsInCategory = 1;
        int maximumNoSpendingMonths = 2;
        new UnusualActivityGenerator(minimumTransactionsInCategory, maximumNoSpendingMonths, metricRegistry,
                new DeepLinkBuilderFactory("")).generateActivity(context);

        assertEquals(1, context.getActivities().size());
        assertEquals(1,
                metricRegistry.meter(RECURRING_EXPENSES_METRIC.label("detected", "yes")).getCount());
        assertEquals(1,
                metricRegistry.meter(RECURRING_EXPENSES_METRIC.label("detected", "no")).getCount());
    }

    private Statistic createTemplateStatistic(String statisticType) {
        Statistic statistic = new Statistic();
        // yes, the statistic description will contain an ID of a category
        statistic.setDescription("categoryId");
        statistic.setType(statisticType);
        statistic.setResolution(ResolutionTypes.MONTHLY);
        return statistic;
    }

    private Map<String, Statistic> createExpensesByCategory(double value, int numberOfMonths) {
        Map<String, Statistic> statistics = new HashMap<>(numberOfMonths);
        for (int i = 1; i <= numberOfMonths; i++) {
            Statistic statistic = createTemplateStatistic(Statistic.Types.EXPENSES_BY_CATEGORY);
            statistic.setPeriod("2016-0" + i);
            statistic.setValue(value);
            statistics.put(statistic.getPeriod(), statistic);
        }
        return statistics;
    }

    private Map<String, Statistic> createCountByCategoryStatistics(double value, int numberOfMonths) {
        final int year = 2016;
        Map<String, Statistic> statistics = new HashMap<>(numberOfMonths);
        for (int i = 1; i <= numberOfMonths; i++) {
            Statistic statistic = createTemplateStatistic(Statistic.Types.EXPENSES_COUNT_BY_CATEGORY);
            statistic.setPeriod(String.format("%d%01d", year, i));
            statistic.setValue(value);
            statistics.put(statistic.getPeriod(), statistic);
        }
        return statistics;
    }

    int maximumDistance = 4;
    int minimumDistance = 1;
    int minPatternMonths = 3;
    double variation = 0.2;

    @SuppressWarnings("unused")
    double[][] recurringExpenses() {
        return new double[][]{
                {1, 1, 9, 9, 9},                // monthly
                {9, 1, 9, 1, 9},                // every second month
                {9, 1, 1, 9, 1, 1, 9},          // every third month
                {9, 1, 1, 1, 9, 1, 1, 1, 9},    // every fourth month
                {9, 1, 9, 1, 9},                // every second month with variation
        };
    }

    @Parameters(method = "recurringExpenses")
    @Test public void recurringExpense(double... expenses) {
        assertTrue(UnusualActivityGenerator.currentMonthFollowsRecurringPattern(
                new DescriptiveStatistics(expenses),  variation, minimumDistance, maximumDistance, minPatternMonths));
    }

    @SuppressWarnings("unused")
    double[][] nonRecurringExpenses() {
        return new double[][]{
                {1, 1, 1, 9, 1, 9}, // not enough repeats
                {9, 1, 1, 9, 1, 9}, // not consistent periodicity
                {9, 1, 1, 1, 1, 9}, // too distant repeats
                {1, 7, 1, 7, 1, 9}, // too high variation
                {9, 1, 9},          // too few data points
        };
    }

    @Parameters(method = "nonRecurringExpenses")
    @Test public void nonRecurringExpense(double... expenses) {
        assertFalse(UnusualActivityGenerator.currentMonthFollowsRecurringPattern(
                new DescriptiveStatistics(expenses), variation, minimumDistance, maximumDistance, minPatternMonths));
    }

}
