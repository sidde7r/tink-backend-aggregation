package se.tink.backend.main.controllers;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.common.providers.CurrenciesByCodeProvider;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.MortgageCalculator;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Currency;
import se.tink.backend.core.MortgageDistribution;
import se.tink.backend.core.MortgageMeasure;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticQuery;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.insights.AmountByCategoryCode;
import se.tink.backend.core.insights.AmountByWeekday;
import se.tink.backend.core.insights.Categories;
import se.tink.backend.core.insights.DailySpending;
import se.tink.backend.core.insights.HistogramBucket;
import se.tink.backend.core.insights.InsightsResponse;
import se.tink.backend.core.insights.LeftToSpend;
import se.tink.backend.core.insights.LeftToSpendByPeriod;
import se.tink.backend.core.insights.Mortgage;
import se.tink.backend.core.insights.Savings;
import se.tink.backend.main.histograms.SavingDistribution;
import se.tink.backend.main.i18n.InsightsLocalizableKeys;
import se.tink.backend.utils.Doubles;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.math.MathUtils;

public class InsightsController {
    private static final int CATEGORIES_COUNT = 3;
    private static final int DAYS_FOR_AVERAGE_DAILY_STATISTICS = 90; // 3 months
    private static final int MONTHS_FOR_LEFT_TO_SPEND_STATISTICS = 6;
    private static final double LEFT_TO_SPEND_MIN_THRESHOLD_PERCENTAGE = 0.1;
    private static final int MINIMUM_SAVING_BALANCE = 10000;
    private final List<HistogramBucket> mortgageDistribution;
    private final AccountServiceController accountServiceController;
    private final StatisticsServiceController statisticsServiceController;
    private final MortgageCalculator mortgageCalculator;
    private final SavingDistribution savingDistribution;
    private final BiMap<String, String> categoryCodeById;
    private final CurrenciesByCodeProvider currenciesByCodeProvider;
    private final CategoryConfiguration categoryConfiguration;
    private final InsightsLocalizableKeys insightsLocalizableKeys;

    @Inject
    public InsightsController(AccountServiceController accountServiceController,
            StatisticsServiceController statisticsServiceController,
            MortgageCalculator mortgageCalculator,
            MortgageDistribution mortgageDistribution,
            SavingDistribution savingDistribution,
            @Named("categoryCodeById") BiMap<String, String> categoryCodeById,
            CurrenciesByCodeProvider currenciesByCodeProvider,
            CategoryConfiguration categoryConfiguration,
            InsightsLocalizableKeys insightsLocalizableKeys) {
        this.accountServiceController = accountServiceController;
        this.statisticsServiceController = statisticsServiceController;
        this.mortgageCalculator = mortgageCalculator;
        this.savingDistribution = savingDistribution;
        this.categoryCodeById = categoryCodeById;
        this.currenciesByCodeProvider = currenciesByCodeProvider;

        this.mortgageDistribution = mortgageDistribution.getBuckets().stream()
                .map(bucket -> new HistogramBucket(bucket.getRange().lowerEndpoint(), bucket.getRange().upperEndpoint(),
                        bucket.getValue()))
                .collect(Collectors.toList());
        this.categoryConfiguration = categoryConfiguration;
        this.insightsLocalizableKeys = insightsLocalizableKeys;
    }

    public InsightsResponse getInsights(User user) {
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
        InsightsResponse response = new InsightsResponse();
        response.setMortgage(buildMortgage(user, catalog));
        response.setCategories(buildCategories(user, catalog));
        response.setDailySpending(buildDailySpending(user, catalog));
        response.setLeftToSpend(buildLeftToSpend(user, catalog));
        response.setSavings(buildSavings(user, catalog));
        return response;
    }

    private DailySpending buildDailySpending(User user, Catalog catalog) {
        Map<Integer, Double> averageDailyAmount;
        try {
            averageDailyAmount = getAverageDailyAmount(getDailyExpenses(user, DAYS_FOR_AVERAGE_DAILY_STATISTICS));
        } catch (LockException e) {
            return null;
        }

        if (averageDailyAmount.isEmpty()) {
            return null;
        }

        Locale locale = I18NUtils.getLocale(user.getLocale());
        Currency currency = currenciesByCodeProvider.get().get(user.getProfile().getCurrency());

        List<AmountByWeekday> amountByWeekdays = averageDailyAmount.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> new AmountByWeekday(I18NUtils.getWeekdayShortName(catalog, locale, entry.getKey()),
                        Math.abs(entry.getValue())))
                .collect(Collectors.toList());

        // Rotate list, so the first element would be the localized weekday (Sunday by default)
        int firstDayOfWeek = Calendar.getInstance(locale).getFirstDayOfWeek();
        Collections.rotate(amountByWeekdays, -(firstDayOfWeek - 1));

        DailySpending dailySpending = new DailySpending();
        dailySpending.setTitle(getDailySpendingTitle(currency, locale, catalog, amountByWeekdays));
        dailySpending.setBody(getDailySpendingBody(currency, locale, catalog, amountByWeekdays));
        dailySpending.setWeekdays(amountByWeekdays);

        return dailySpending;
    }

    private String getDailySpendingTitle(Currency currency, Locale locale, Catalog catalog,
            List<AmountByWeekday> amountByWeekdays) {
        double avgSpending = amountByWeekdays.stream().mapToDouble(AmountByWeekday::getAmount).average().orElse(0);
        return Catalog.format(catalog.getString(insightsLocalizableKeys.getDailySpendingTitle()),
                I18NUtils.formatCurrency(avgSpending, currency, locale));
    }

    private String getDailySpendingBody(Currency currency, Locale locale, Catalog catalog,
            List<AmountByWeekday> amountByWeekdays) {
        AmountByWeekday biggestSpending = amountByWeekdays.stream()
                .max(Comparator.comparing(AmountByWeekday::getAmount)).get();
        String weekdayFullName;
        try {
            DayOfWeek dayOfWeek = I18NUtils.getDayOfWeek(locale, biggestSpending.getWeekday());
            weekdayFullName = I18NUtils.getPluralizedWeekdayName(catalog, dayOfWeek);
        } catch (ParseException e) {
            weekdayFullName = biggestSpending.getWeekday();
        }

        return Catalog.format(catalog.getString(insightsLocalizableKeys.getDailySpendingBody()),
                weekdayFullName, I18NUtils.formatCurrency(Math.abs(biggestSpending.getAmount()), currency, locale));
    }

    private List<Statistic> getDailyExpenses(User user, int lastDays) throws LockException {
        StatisticQuery statisticQuery = new StatisticQuery();
        UserProfile userProfile = user.getProfile();
        statisticQuery.setResolution(ResolutionTypes.DAILY);
        statisticQuery.setTypes(Collections.singletonList(Statistic.Types.INCOME_AND_EXPENSES));

        List<Statistic> statistics = statisticsServiceController
                .query(user.getId(), userProfile.getPeriodMode(), statisticQuery);

        String startDate = DateUtils.toDayPeriod(DateTime.now().minusDays(lastDays));

        return statistics.stream()
                .filter(statistic -> Objects.equals(statistic.getDescription(), CategoryTypes.EXPENSES.name()))
                .filter(statistic -> statistic.getPeriod().compareTo(startDate) >= 0)
                .collect(Collectors.toList());
    }

    private Map<Integer, Double> getAverageDailyAmount(List<Statistic> statistics) {
        return statistics.stream()
                .map(statistic -> new StringDoublePair(statistic.getPeriod(), statistic.getValue()))
                .collect(Collectors
                        .groupingBy(pair -> DateUtils.getCalendar(DateUtils.parseDate(pair.getKey()))
                                .get(Calendar.DAY_OF_WEEK), Collectors.averagingDouble(StringDoublePair::getValue)));
    }

    private Mortgage buildMortgage(User user, Catalog catalog) {
        if (mortgageDistribution.isEmpty()) {
            return null;
        }
        MortgageMeasure mortgageMeasure = mortgageCalculator.aggregate(accountServiceController.list(user.getId()));
        if (Doubles.fuzzyEquals(mortgageMeasure.getAmount(), 0D, 0.01)) {
            return null;
        }

        Mortgage mortgage = new Mortgage();
        mortgage.setTitle(getMortgageTitle(catalog, BigDecimal.valueOf(mortgageMeasure.getRate())));
        mortgage.setBody(catalog.getString(insightsLocalizableKeys.getMortgageBody()));
        mortgage.setDistribution(mortgageDistribution);
        mortgage.setInterestRate(mortgageMeasure.getRate());

        return mortgage;
    }

    private String getMortgageTitle(Catalog catalog, BigDecimal mortgageRate) {
        if (MortgageDistribution.AVERAGE_RATE.compareTo(mortgageRate) <= 0) {
            return catalog.getString(insightsLocalizableKeys.getMortgageTitleHighRate());
        }

        if (MortgageDistribution.LOW_RATE.compareTo(mortgageRate) > 0) {
            return catalog.getString(insightsLocalizableKeys.getMortgageTitleLowRate());
        } else {
            return catalog.getString(insightsLocalizableKeys.getMortgageTitleMediumRate());
        }
    }

    private Categories buildCategories(User user, Catalog catalog) {
        Categories categories = new Categories();
        categories.setTitle(catalog.getString(insightsLocalizableKeys.getCategoriesTitle()));
        categories.setBody(catalog.getString(insightsLocalizableKeys.getCategoriesBody()));
        try {
            List<AmountByCategoryCode> expenses = getLastMonthMostSpendCategory(user, CATEGORIES_COUNT)
                    .stream()
                    .map(statistic -> new AmountByCategoryCode(categoryCodeById.get(statistic.getDescription()),
                            Math.abs(statistic.getValue())))
                    .sorted(Comparator.comparing(AmountByCategoryCode::getAmount).reversed())
                    .collect(Collectors.toList());
            categories.setHighestExpensesLastPeriod(expenses);
        } catch (LockException e) {
            return null;
        }

        return categories;
    }

    private List<Statistic> getLastMonthMostSpendCategory(User user, int limit) throws LockException {
        StatisticQuery statisticQuery = new StatisticQuery();
        UserProfile userProfile = user.getProfile();
        statisticQuery.setPeriods(
                Collections.singletonList(DateUtils.getPreviousMonthPeriod(DateUtils
                        .getCurrentMonthPeriod(userProfile.getPeriodMode(), userProfile.getPeriodAdjustedDay()))));
        statisticQuery.setResolution(userProfile.getPeriodMode());
        statisticQuery.setTypes(Collections.singletonList(Statistic.Types.EXPENSES_BY_CATEGORY));

        List<Statistic> statistics = statisticsServiceController
                .query(user.getId(), userProfile.getPeriodMode(), statisticQuery);

        return statistics.stream()
                .sorted(Comparator.comparingDouble(Statistic::getValue))
                .filter(statistic -> !(Objects.equals(categoryCodeById.get(statistic.getDescription()), categoryConfiguration.getExpenseUnknownCode())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private LeftToSpend buildLeftToSpend(User user, Catalog catalog) {
        List<String> periods = DateUtils.createMonthlyPeriodList(
                DateUtils.getPreviousMonthPeriod(UserProfile.ProfileDateUtils.getCurrentMonthPeriod(user.getProfile())),
                MONTHS_FOR_LEFT_TO_SPEND_STATISTICS);

        List<LeftToSpendByPeriod> leftToSpendByPeriods;

        try {
            Map<String, Double> monthlyIncome = getMonthlyIncome(user, periods).stream()
                    .collect(Collectors.toMap(Statistic::getPeriod, Statistic::getValue));

            leftToSpendByPeriods = getMonthlyLeftToSpend(user, periods).stream()
                    .map(statistic -> {

                        double income = monthlyIncome.getOrDefault(statistic.getPeriod(), 0D);
                        double percentage = Doubles.fuzzyEquals(income, 0D, 0.01) ? 0D : statistic.getValue() / income;
                        return new LeftToSpendByPeriod(statistic.getPeriod(), percentage);
                    })
                    .sorted(Comparator.comparing(LeftToSpendByPeriod::getPeriod))
                    .collect(Collectors.toList());
        } catch (LockException e) {
            return null;
        }

        if (!isHaveSavingMoney(leftToSpendByPeriods, LEFT_TO_SPEND_MIN_THRESHOLD_PERCENTAGE)) {
            return null;
        }

        LeftToSpend leftToSpend = new LeftToSpend();
        leftToSpend.setTitle(catalog.getString(insightsLocalizableKeys.getLeftToSpendTitleHaveMoney()));
        leftToSpend.setBody(catalog.getString(insightsLocalizableKeys.getLeftToSpendBodyHaveMoney()));
        leftToSpend.setMostRecentPeriods(leftToSpendByPeriods);
        return leftToSpend;
    }

    private List<Statistic> getMonthlyLeftToSpend(User user, List<String> periods) throws LockException {
        StatisticQuery statisticQuery = new StatisticQuery();
        UserProfile userProfile = user.getProfile();
        statisticQuery.setPeriods(periods);
        statisticQuery.setResolution(userProfile.getPeriodMode());
        statisticQuery.setTypes(Collections.singletonList(Statistic.Types.LEFT_TO_SPEND));

        return Lists.newArrayList(
                statisticsServiceController.query(user.getId(), userProfile.getPeriodMode(), statisticQuery).stream()
                        .collect(Collectors.groupingBy(Statistic::getPeriod,
                                Collectors.collectingAndThen(Collectors.toList(), this::pickLastLeftToSpendStatistic)))
                        .values());
    }

    private Statistic pickLastLeftToSpendStatistic(List<Statistic> statistics) {
        return statistics.stream()
                .max(Comparator.comparing(Statistic::getDescription))
                .orElse(new Statistic());
    }

    private List<Statistic> getMonthlyIncome(User user, List<String> periods) throws LockException {
        StatisticQuery statisticQuery = new StatisticQuery();
        UserProfile userProfile = user.getProfile();
        statisticQuery.setPeriods(periods);
        statisticQuery.setResolution(userProfile.getPeriodMode());
        statisticQuery.setTypes(Collections.singletonList(Statistic.Types.INCOME_AND_EXPENSES));

        List<Statistic> statistics = statisticsServiceController
                .query(user.getId(), userProfile.getPeriodMode(), statisticQuery);

        return statistics.stream()
                .filter(statistic -> Objects.equals(statistic.getDescription(), CategoryTypes.INCOME.name()))
                .collect(Collectors.toList());
    }

    private boolean isHaveSavingMoney(List<LeftToSpendByPeriod> leftToSpendByPeriods, double threshold) {
        return leftToSpendByPeriods.stream()
                .map(LeftToSpendByPeriod::getPercentage)
                .filter(percentage -> percentage >= threshold)
                .count() > leftToSpendByPeriods.size() / 2;
    }

    private Savings buildSavings(User user, Catalog catalog) {
        List<Account> userAccounts = accountServiceController.list(user.getId());
        double savingsAccountsBalance = sumAmountByAccountType(userAccounts, AccountTypes.SAVINGS);
        double investmentAccountsBalance = sumAmountByAccountType(userAccounts, AccountTypes.INVESTMENT);
        double totalSavingsAndInvestments = savingsAccountsBalance + investmentAccountsBalance;

        if (savingsAccountsBalance < MINIMUM_SAVING_BALANCE
                && totalSavingsAndInvestments < SavingDistribution.AVERAGE_SAVING_AMOUNT) {
            return null;
        }

        // If aggregated amount is more than average savings amount, use it, otherwise, use only balances from savings accounts
        double amount = totalSavingsAndInvestments > SavingDistribution.AVERAGE_SAVING_AMOUNT ?
                totalSavingsAndInvestments :
                savingsAccountsBalance;
        Locale locale = I18NUtils.getLocale(user.getLocale());

        Savings savings = new Savings();
        savings.setAmount(amount);
        savings.setTitle(getSavingsTitle(locale, catalog, amount));
        savings.setBody(catalog.getString(insightsLocalizableKeys.getSavingsBody()));
        return savings;
    }

    private String getSavingsTitle(Locale locale, Catalog catalog, double amount) {

        NumberFormat percentFormat = NumberFormat.getPercentInstance(locale);
        percentFormat.setRoundingMode(RoundingMode.HALF_DOWN);

        if (amount > SavingDistribution.AVERAGE_SAVING_AND_INVESTMENT_AMOUNT) {

            BigDecimal percentile = savingDistribution
                    .findBucket(amount)
                    .map(SavingDistribution.Bucket::getPercentile)
                    .orElse(BigDecimal.valueOf(0.99));

            BigDecimal top = BigDecimal.ONE.subtract(percentile);

            BigDecimal resolution;
            if (top.compareTo(BigDecimal.valueOf(3,2)) < 0) {
                resolution = BigDecimal.valueOf(1,2);
            } else {
                resolution = BigDecimal.valueOf(5,2);
            }

            top = MathUtils.ceiling(top, resolution);

            return Catalog
                    .format(catalog.getString(insightsLocalizableKeys.getSavingsTitleMoreThanAverageWithInvestments()),
                            percentFormat.format(top.doubleValue()));
        } else if (amount > SavingDistribution.AVERAGE_SAVING_AMOUNT) {

            BigDecimal percentMoreThanAverage  = BigDecimal.valueOf(amount)
                    .divide(BigDecimal.valueOf(SavingDistribution.AVERAGE_SAVING_AMOUNT), MathContext.DECIMAL32)
                    .subtract(BigDecimal.ONE);

            BigDecimal resolution;
            if (percentMoreThanAverage.compareTo(BigDecimal.valueOf(5,2)) < 0) {
                resolution = BigDecimal.valueOf(1,2);
            } else {
                resolution = BigDecimal.valueOf(5,2);
            }

            percentMoreThanAverage = MathUtils.floor(percentMoreThanAverage, resolution);

            return Catalog.format(catalog.getString(insightsLocalizableKeys.getSavingsTitleMoreThanAverage()),
                    percentFormat.format(percentMoreThanAverage.doubleValue()));
        } else {
            return catalog.getString(insightsLocalizableKeys.getSavingsTitleLessThanAverage());
        }
    }

    private double sumAmountByAccountType(List<Account> accounts, AccountTypes types) {
        return accounts.stream()
                .filter(account -> Objects.equals(account.getType(), types))
                .mapToDouble(Account::getBalance)
                .sum();
    }
}
