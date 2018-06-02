package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import se.tink.backend.common.config.ActivitiesConfiguration;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.AccountBalanceActivityData;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.Notification;
import se.tink.backend.core.Statistic;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;

public class AccountBalanceActivityGenerator extends ActivityGenerator {

    private static final LogUtils log = new LogUtils(AccountBalanceActivityGenerator.class);

    private static final double HIGH_PERCENTILE_THRESHOLD = 99;
    private static final double LOW_PERCENTILE_THRESHOLD = 10;
    private static final int MAX_NBR_OF_DAYS = 90;
    private static final double MIN_HIGH_BALANCE_THRESHOLD = 500;
    private static final double MIN_LOW_BALANCE_THRESHOLD = 500;
    private static final int MIN_NBR_OF_DAYS = 31;
    private static final double PEAK_TO_DROUGHT_RATIO = 0.9;
    private static final double DROUGHT_TO_PEAK_RATIO = 3;

    private DeepLinkBuilderFactory deepLinkBuilderFactory;

    private static AccountBalanceActivityData createAccountBalanceData(Account account,
            List<Statistic> accountBalanceStatistics) {

        AccountBalanceActivityData content = new AccountBalanceActivityData();

        content.setAccount(account);
        content.setData(Lists.newArrayList(Iterables.transform(accountBalanceStatistics,
                s -> new KVPair<>(s.getPeriod(), s.getValue()))));

        return content;
    }

    public AccountBalanceActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(AccountBalanceActivityGenerator.class, 60, 80, deepLinkBuilderFactory);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        ActivitiesConfiguration activitiesConfiguration = context.getActivitiesConfiguration();

        // Generate only for non-excluded accounts of specific types (that generally have a positive balance).
        Iterable<Account> accounts = Iterables.filter(context.getAccounts(), account -> {
            if (account.isExcluded()) {
                return false;
            }

            switch (account.getType()) {
            case CHECKING:
            case SAVINGS:
            case OTHER:
                return true;

            case CREDIT_CARD:
            case INVESTMENT:
            case LOAN:
            case MORTGAGE:
            case PENSION:
            default:
                return false;
            }
        });

        // The predicate includes `balances-by-account` statistics with `DAILY` resolution. For such statistic objects,
        // the description consists of the account id.
        ImmutableListMultimap<String, Statistic> accountBalanceStatisticsByAccountId = Multimaps.index(
                Iterables.filter(context.getStatistics(), Predicates.DAILY_ACCOUNT_BALANCE_STATISTICS),
                Statistic::getDescription);

        final double minimumLowBalanceThreshold = context.getUserCurrency().getFactor() * MIN_LOW_BALANCE_THRESHOLD;
        final double minimumHighBalanceThreshold = context.getUserCurrency().getFactor() * MIN_HIGH_BALANCE_THRESHOLD;

        DescriptiveStatistics statistics = new DescriptiveStatistics(MAX_NBR_OF_DAYS);
        Date beginningOfPeriod = DateUtils.addDays(new Date(), -30);

        for (final Account account : accounts) {

            statistics.clear();

            List<Statistic> accountBalanceStatistics = accountBalanceStatisticsByAccountId.get(account.getId())
                    .stream().sorted(Comparator.comparing(Statistic::getPeriod)).collect(Collectors.toList());

            for (Statistic accountBalanceEntry : accountBalanceStatistics) {
                statistics.addValue(accountBalanceEntry.getValue());
            }

            // Skip accounts with negative mean balance.
            if (statistics.getMean() < 0) {
                continue;
            }

            statistics.clear();

            // Flag to indicate going in and out of the warning zone.

            boolean isLowWarning = false;
            Double peakAmount = null;
            Double lowWarningAmount = null;

            boolean isHighWarning = false;
            Double droughtAmount = null;
            Double highWarningAmount = null;

            // Loop through the historical account balances and compute our thresholds and balances.

            for (int i = 0; i < accountBalanceStatistics.size(); i++) {
                Statistic accountBalanceEntry = accountBalanceStatistics.get(i);
                final double balance = accountBalanceEntry.getValue();
                final double highBalanceThreshold = statistics.getPercentile(HIGH_PERCENTILE_THRESHOLD);
                final double lowBalanceThreshold = statistics.getPercentile(LOW_PERCENTILE_THRESHOLD);
                final Date date;
                {
                    try {
                        date = ThreadSafeDateFormat.FORMATTER_DAILY.parse(accountBalanceEntry.getPeriod());
                    } catch (ParseException e) {
                        log.warn(accountBalanceEntry.getUserId(), "Unable to parse date.", e);
                        continue;
                    }
                }

                statistics.addValue(Math.max(balance, 0));

                peakAmount = (peakAmount == null ? balance : Math.max(peakAmount, balance));
                droughtAmount = (droughtAmount == null ? balance : Math.min(droughtAmount, balance));

                if (statistics.getValues().length <= MIN_NBR_OF_DAYS) {
                    continue;
                }

                // Low balance warnings are only relevant for the last month.

                if (date.before(beginningOfPeriod)) {
                    continue;
                }

                // Evaluate if we should generate a low-balance activity.

                boolean shouldGenerateLowWarning = activitiesConfiguration.shouldGenerateLowBalanceWarning();
                // Balance in the lower percentiles of historical account balances.
                shouldGenerateLowWarning &= (balance < lowBalanceThreshold);
                // Balance lower than a fixed threshold.
                shouldGenerateLowWarning &= (balance < minimumLowBalanceThreshold);
                // Balance lower than a specified fraction of the peak amount.
                shouldGenerateLowWarning &= (balance < (PEAK_TO_DROUGHT_RATIO * peakAmount));

                if (shouldGenerateLowWarning) {
                    if (!isLowWarning) {
                        AccountBalanceActivityData content = createAccountBalanceData(account,
                                accountBalanceStatistics.subList(i - MIN_NBR_OF_DAYS + 1, i + 1));

                        String key = String.format("%s.%s.%s", Activity.Types.BALANCE_LOW, dateToString(date),
                                account.getId());

                        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

                        context.addActivity(
                                createActivity(
                                        context.getUser().getId(),
                                        date,
                                        Activity.Types.BALANCE_LOW,
                                        context.getCatalog().getString("Low balance"),
                                        generateLowBalanceMessage(context, account),
                                        content,
                                        key,
                                        feedActivityIdentifier));
                    }

                    isLowWarning = true;
                    lowWarningAmount = balance;
                } else if (lowWarningAmount != null && balance > lowWarningAmount) {
                    if (isLowWarning) {
                        peakAmount = balance;
                    }

                    isLowWarning = false;
                    lowWarningAmount = null;
                }

                // Evaluate if we should generate a high-balance activity.

                boolean shouldGenerateHighWarning = activitiesConfiguration.shouldGenerateHighBalanceWarning();
                // Balance in the higher percentiles of historical account balances.
                shouldGenerateHighWarning &= (balance > highBalanceThreshold);
                // Balance higher than a fixed threshold.
                shouldGenerateHighWarning &= (balance > minimumHighBalanceThreshold);
                // Balance higher than a specified multiple of the drought amount.
                shouldGenerateHighWarning &= (balance > (DROUGHT_TO_PEAK_RATIO * droughtAmount));

                if (shouldGenerateHighWarning) {
                    if (!isHighWarning) {

                        AccountBalanceActivityData content = createAccountBalanceData(account,
                                accountBalanceStatistics.subList(i - MIN_NBR_OF_DAYS + 1, i + 1));

                        String key = String.format("%s.%s.%s", Activity.Types.BALANCE_HIGH, dateToString(date),
                                account.getId());

                        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

                        context.addActivity(
                                createActivity(
                                        context.getUser().getId(),
                                        date,
                                        Activity.Types.BALANCE_HIGH,
                                        context.getCatalog().getString("High balance"),
                                        generateHighBalanceMessage(context, account),
                                        content,
                                        key,
                                        feedActivityIdentifier));
                    }

                    isHighWarning = true;
                    highWarningAmount = balance;
                } else if (highWarningAmount != null && balance < highWarningAmount) {
                    if (isHighWarning) {
                        droughtAmount = balance;
                    }

                    isHighWarning = false;
                    highWarningAmount = null;
                }
            }
        }
    }

    private static String generateHighBalanceMessage(ActivityGeneratorContext context, Account account) {
        final Catalog catalog = context.getCatalog();
        boolean shouldGenerateSensitiveMessage = context.getActivitiesConfiguration().shouldGenerateSensitiveMessage();

        if (shouldGenerateSensitiveMessage) {
            return Catalog.format(catalog.getString("You have an unusually high balance on {0}"), account.getName());
        } else {
            return catalog.getString("You have an unusually high balance on one account.");
        }
    }

    private static String generateLowBalanceMessage(ActivityGeneratorContext context, Account account) {
        final Catalog catalog = context.getCatalog();
        boolean shouldGenerateSensitiveMessage = context.getActivitiesConfiguration().shouldGenerateSensitiveMessage();

        if (shouldGenerateSensitiveMessage) {
            return Catalog.format(catalog.getString("You have an unusually low balance on {0}"), account.getName());
        } else {
            return catalog.getString("You have an unusually low balance on one account.");
        }
    }

    @Override
    protected List<Notification> createNotifications(Activity activity, ActivityGeneratorContext context) {
        Object activityContent = activity.getContent();
        if (!(activityContent instanceof AccountBalanceActivityData)) {
            return Lists.newArrayList();
        }

        AccountBalanceActivityData accountData = (AccountBalanceActivityData) activityContent;

        Notification.Builder notification = new Notification.Builder()
                .fromActivity(activity)
                .groupable(true);

        if (accountData.getAccount() != null) {
            if (context.getCluster() == Cluster.CORNWALL) {
                notification.url(String
                        .format("%saccount/%s", deepLinkBuilderFactory.getPrefix(), accountData.getAccount().getId()));
            } else {
                notification.url(deepLinkBuilderFactory.account(accountData.getAccount().getId()).build());
            }
        }

        return buildNotificationsSilentlyFailing(activity.getUserId(), notification);
    }

    private static String dateToString(Date date) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(date);
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
