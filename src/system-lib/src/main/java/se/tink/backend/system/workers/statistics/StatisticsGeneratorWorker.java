package se.tink.backend.system.workers.statistics;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang.time.StopWatch;
import org.joda.time.DateTime;
import org.joda.time.Months;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.config.StatisticConfiguration;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.common.i18n.SocialSecurityNumber.Sweden;
import se.tink.backend.common.statistics.PeriodCalculator;
import se.tink.backend.common.statistics.StatisticsGenerator;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.common.tracking.TrackableEvent;
import se.tink.backend.common.tracking.appsflyer.AppsFlyerEventBuilder;
import se.tink.backend.common.tracking.appsflyer.AppsFlyerTracker;
import se.tink.backend.common.utils.CommonStringUtils;
import se.tink.backend.common.utils.PaydayCalculator;
import se.tink.backend.common.workers.statistics.AbstractGeneratorWorker;
import se.tink.backend.common.workers.statistics.account.AccountBalanceHistoryCalculator;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.CassandraStatistic;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.DeletedUser;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.Provider;
import se.tink.backend.core.StatisticContainer;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserDemographics;
import se.tink.backend.core.UserOrigin;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserState;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.enums.Gender;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.system.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.TagsUtils;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricBuckets;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.SequenceTimer;
import se.tink.libraries.metrics.SequenceTimers;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.metrics.Timer.Context;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * Generates statistics based on user data such as transactions, accounts, credentials etc. Load all the user's data
 * from the database, calculates everything in-memory, and injects the statistics back to the database.
 */
public class StatisticsGeneratorWorker extends AbstractGeneratorWorker {

    private static class GenerateStatisticsTimers {
        private static final String INVALIDATE_CACHE = "invalidate-cache";
        private static final String BALANCE_HISTORY = "balance_history";
        private static final String STATISTIC_TYPES = "statistic_types";
        private static final String GENERATE_USER_STATE = "generate_user_state";
        private static final String GET_USER_DATA = "get_user_data";
        private static final String PERSIST = "persist";
    }

    private static class GenerateUserStateTimers {
        private static final String CALCULATE_CATEGORIZATION = "calculate_categorization";
        private static final String CALCULATE_FAVORED_ACCOUNTS = "calculate_favored_accounts";
        private static final String CALCULATE_MERCHANTIZATION = "calculate_merchantization";
        private static final String CALCULATE_PAYDAY = "calculate_payday";
        private static final String CALCULATE_PERIODS = "calculate_periods";
        private static final String EXTRACT_TAGS = "extract_tags";
        private static final String PERSIST = "persist";
        private static final String PREPARE = "prepare";
    }

    private static final MetricId GENERATE_USER_STATISTICS_LOAD_USER_DATA =
            MetricId.newId("statistics_load_data");
    private static final MetricId GENERATE_USER_STATISTICS_LOAD_USER_DATA_TRANSACTIONS =
            MetricId.newId("statistics_load_data_transactions");
    private static final MetricId REFRESH_TIMER = MetricId.newId("refresh_credentials");

    private static final MetricId AMOUNT_PERCENTAGE_INITIAL = MetricId
            .newId("categorization_amount_percentage_initial");
    private static final MetricId TRANSACTION_PERCENTAGE_INITIAL =
            MetricId.newId("categorization_transactions_percentage_initial");
    private static final MetricId AMOUNT_LESS_THAN_10K_PERCENTAGE_INITIAL =
            MetricId.newId("categorization_amount_expenses_under_10k_percentage_initial");

    private static final LogUtils log = new LogUtils(StatisticsGeneratorWorker.class);
    private static final Ordering<CredentialsEvent> CREDENTIALS_EVENT_BY_DATE = new Ordering<CredentialsEvent>() {
        @Override
        public int compare(CredentialsEvent ae1, CredentialsEvent ae2) {
            return ae1.getTimestamp().compareTo(ae2.getTimestamp());
        }
    };

    private static final Ordering<Entry<String, Collection<Transaction>>> TRANSACTION_COLLECTION_ENTRY_SORTING_BY_COUNT = new Ordering<Entry<String, Collection<Transaction>>>() {
        @Override
        public int compare(Entry<String, Collection<Transaction>> t1, Entry<String, Collection<Transaction>> t2) {
            return Ints.compare(t1.getValue().size(), t2.getValue().size());
        }
    };

    private static final Ordering<Transaction> TRANSACTION_SORTING_BY_DATE = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction t1, Transaction t2) {
            return t1.getOriginalDate().compareTo(t2.getOriginalDate());
        }
    };

    private final Histogram initialCategorizationAmountLevelHistogram;
    private final Histogram initialCategorizationCountLevelHistogram;
    private final Histogram initialExpensesLessThan10kCategorizationLevelHistogram;

    private static final int USER_ACTIVITY_FREQ_MAX = 5;
    private static final int USER_ACTIVITY_FREQ_MIN = 1;
    private final StatisticsGenerator statisticsGenerator;
    private final FirehoseQueueProducer firehoseQueueProducer;
    private final String uncategorizedCategoryId;

    private static final Date EARLIEST_DATE_ALLOWED = new Date(0);

    // TODO: Improve method name. Too general.
    public static double calculateNegativeFunctionValue(double x, int threshold) {
        if (x > threshold) {
            return USER_ACTIVITY_FREQ_MIN;
        }
        return -4 * x / threshold + 5;
    }

    // TODO: Improve method name. Too general.
    public static double calculatePositiveFunctionValue(double value, double threshold) {
        if (value > threshold) {
            return USER_ACTIVITY_FREQ_MAX;
        }
        return 4 * value / threshold + 1;
    }

    private static Iterable<Transaction> getNonExcludedTransactions(UserData context) {
        final ImmutableMap<String, Account> accountsById = Maps.uniqueIndex(context.getAccounts(),
                Account::getId);

        return Iterables.filter(context.getTransactions(), t -> {
            if (!accountsById.containsKey(t.getAccountId())) {
                return false;
            }
            return (!accountsById.get(t.getAccountId()).isExcluded());
        });
    }

    private class MerchantLevelStatistics {
        int MerchantLevel;
        int MerchantWithLocationLevel;
    }

    public UserDemographics calculateUserDemographics(UserDemographics demographics, UserData userData,
            Optional<DeletedUser> deletedUser, final Map<String, Provider> providersByName) {

        // First updated event.

        demographics.setFirstUpdatedEvent(Iterables.getFirst(Iterables.transform(Iterables.filter(
                CREDENTIALS_EVENT_BY_DATE.sortedCopy(userData.getCredentialsEvents()),
                ce -> ce.getStatus() == CredentialsStatus.UPDATED), CredentialsEvent::getTimestamp), null));

        // If user is null, the user is deleted keep old values and return.

        if (userData.getUser() == null) {
            demographics.setDeleted(deletedUser.get().getInserted());
            return demographics;
        }

        demographics.setCreated(userData.getUser().getCreated());
        demographics.setFlags(Sets.newHashSet(userData.getUser().getFlags()));
        demographics.setHasPassword(userData.getUser().getHash() != null);
        demographics.setHasFacebook(userData.getUserFacebookProfile() != null);

        // Credentials data.

        demographics.setProviders(Lists.newArrayList(Iterables.transform(userData.getCredentials(),
                Credentials::getProviderName)));

        demographics.setCredentialsCount(Iterables.size(userData.getCredentials()));
        demographics.setValidCredentialsCount(Iterables.size(Iterables.filter(userData.getCredentials(),
                c -> c.getStatus() == CredentialsStatus.UPDATED)));

        // To decide the latest update event, take the oldest of each credentials' latest UPDATED event. 
        // Exclude DISABLED and non-transactional credentials.

        ImmutableListMultimap<String, CredentialsEvent> agentEventsByCredentialsId = Multimaps.index(
                Iterables.filter(userData.getCredentialsEvents(),
                        input -> Objects.equal(input.getStatus(), CredentialsStatus.UPDATED)),
                ae -> UUIDUtils.toTinkUUID(ae.getCredentialsId()));

        List<CredentialsEvent> lastUpdatedForEachCredentials = Lists.newArrayList();

        for (Credentials credentials : userData.getCredentials()) {

            if (credentials.getType() == CredentialsTypes.FRAUD) {
                continue;
            }

            if (credentials.getStatus() == CredentialsStatus.DISABLED) {
                continue;
            }

            if (!providersByName.containsKey(credentials.getProviderName()) ||
                    !providersByName.get(credentials.getProviderName()).isTransactional()) {
                continue;
            }

            List<CredentialsEvent> agentEvents = agentEventsByCredentialsId.get(credentials.getId());

            if (agentEvents == null || agentEvents.size() == 0) {
                continue;
            }

            CredentialsEvent latestEvent = CREDENTIALS_EVENT_BY_DATE.max(agentEvents);
            lastUpdatedForEachCredentials.add(latestEvent);

        }

        if (lastUpdatedForEachCredentials.size() != 0) {
            CredentialsEvent lastUpdatedEvent = CREDENTIALS_EVENT_BY_DATE.min(lastUpdatedForEachCredentials);

            if (lastUpdatedEvent != null) {
                demographics.setLastUpdatedEvent(lastUpdatedEvent.getTimestamp());
            }
        }

        // Find agent error frequency.

        final Calendar calendar = Calendar.getInstance();

        ImmutableListMultimap<String, CredentialsEvent> agentEventsByWeek = Multimaps
                .index(userData.getCredentialsEvents(),
                        ce -> {
                            calendar.setTime(ce.getTimestamp());
                            return String.valueOf(calendar.get(Calendar.YEAR)) + "-"
                                    + String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR));
                        });

        double numberOfErrorWeeks = 0;
        double numberOfAuthErrorWeeks = 0;

        if (agentEventsByWeek.size() != 0) {
            weekLoop:
            for (String week : agentEventsByWeek.keySet()) {
                for (CredentialsEvent ae : agentEventsByWeek.get(week)) {
                    if (ae.getStatus() == CredentialsStatus.AUTHENTICATION_ERROR
                            || ae.getStatus() == CredentialsStatus.TEMPORARY_ERROR
                            || ae.getStatus() == CredentialsStatus.PERMANENT_ERROR) {

                        numberOfErrorWeeks++;

                        if (ae.getStatus() == CredentialsStatus.AUTHENTICATION_ERROR) {
                            numberOfAuthErrorWeeks++;
                        }

                        continue weekLoop;
                    }
                }
            }
        }

        demographics.setWeeklyErrorFrequency(numberOfErrorWeeks != 0 ? numberOfErrorWeeks
                / agentEventsByWeek.keySet().size() : null);
        demographics.setWeeklyAuthErrorFrequency(numberOfAuthErrorWeeks != 0 ? numberOfAuthErrorWeeks
                / agentEventsByWeek.keySet().size() : null);

        // Transactions and valid clean data periods.

        demographics.setValidCleanDataPeriodsCount(Iterables.size(DateUtils.getCleanPeriods(userData.getUserState()
                .getPeriods())));
        demographics.setTransactionCount(Iterables.size(userData.getTransactions()));
        demographics.setHasHadTransactions(userData.getUserState().isHaveHadTransactions());

        // Categorization, market, gender and age.

        demographics.setInitialCategorization(userData.getUserState()
                .getInitialExpensesLessThan10kCategorizationLevel());
        demographics.setCurrentCategorization(userData.getUserState().getExpensesLessThan10kCategorizationLevel());
        demographics.setMarket(userData.getUser().getProfile().getMarket());

        int year = Calendar.getInstance().get(Calendar.YEAR);

        if (userData.getUser().getProfile().getBirth() != null && userData.getUser().getProfile().getGender() != null) {
            demographics.setAge(year - Integer.valueOf(userData.getUser().getProfile().getBirth().substring(0, 4)));
            demographics.setBirth(userData.getUser().getProfile().getBirth());
            demographics.setGender(userData.getUser().getProfile().getGender());

        } else {

            Sweden personNumber = SocialSecurityNumber.Sweden
                    .findPersonNumberFromCredentials(userData.getCredentials());

            if (personNumber != null) {

                Gender gender = personNumber.getGender();
                int birthYear = personNumber.getBirthYear();

                if (birthYear != -1) {
                    demographics.setAge(year - birthYear);
                }
                if (gender != null) {
                    demographics.setGender(gender.toLowerCase());
                }
            }
        }

        // Budgets

        int followItemCount = (userData.getFollowItems() != null ? userData.getFollowItems().size() : 0);

        demographics.setFollowItemCount(followItemCount);

        // Income

        Iterable<Transaction> incomeTransactions = Iterables.filter(userData.getTransactions(),
                t -> {
                    if (!categoriesById.containsKey(t.getCategoryId())) {
                        return false;
                    }
                    return categoryConfiguration.getIncomeCodes()
                            .contains(categoriesById.get(t.getCategoryId()).getCode());
                });

        if (Iterables.size(incomeTransactions) != 0) {

            long incomeSum = 0;

            for (Transaction t : incomeTransactions) {
                incomeSum += t.getAmount();
            }

            Date earliestTransactionDate = TRANSACTION_SORTING_BY_DATE.min(incomeTransactions).getDate();
            Date oldestTransactionDate = TRANSACTION_SORTING_BY_DATE.max(incomeTransactions).getDate();
            int monthsBetween = Months.monthsBetween(new DateTime(earliestTransactionDate),
                    new DateTime(oldestTransactionDate)).getMonths();
            demographics.setIncome(incomeSum != 0 ? incomeSum / (monthsBetween + 1) : null);
        }

        // Tags

        long taggedTransactionCount = userData.getTransactions().stream().filter(TagsUtils::hasTags).count();

        int uniqueTagCount = (userData.getUserState().getTags() != null ? userData.getUserState().getTags().size() : 0);

        demographics.setTaggedTransactionCount((int) taggedTransactionCount);
        demographics.setUniqueTagCount(uniqueTagCount);

        // User origin

        UserOrigin origin = userData.getUserOrigin();

        if (origin != null) {
            demographics.setOrganic(origin.isOrganic());
            demographics.setCampaign(origin.getCampaign());
            demographics.setSource(origin.getMediaSource());
        }

        // Postal Code

        if (userData.getFraudDetails() != null) {
            Iterable<FraudDetails> addressDetails = Iterables
                    .filter(userData.getFraudDetails(),
                            fraudDetails -> fraudDetails.getType() == FraudDetailsContentType.ADDRESS);

            if (addressDetails != null && Iterables.size(addressDetails) > 0) {
                FraudDetails latest = Ordering.from(Orderings.FRAUD_DETAILS_DATE).max(addressDetails);

                if (latest != null) {
                    FraudAddressContent content = (FraudAddressContent) latest.getContent();
                    demographics.setPostalCode(content.getPostalcode());
                    demographics.setCommunity(content.getCommunity());
                    demographics.setCity(content.getCity());
                }
            }
        }
        return demographics;
    }

    private final Timer statisticsGenerationLoadUserDataTimer;
    private final Timer statisticsGenerationLoadUserDataTransactionsTimer;
    private final Timer refreshTimerOther;
    private final Timer refreshTimerBankId;
    private final Timer refreshTimerPassword;

    private final SequenceTimer generateStatisticsSequenceTimer;
    private final SequenceTimer generateUserStateSequenceTimer;
    private final SequenceTimer invalidateStatisticsCacheSequenceTimer;

    private final AccountBalanceHistoryCalculator accountBalanceHistoryCalculator;
    private final CacheClient cacheClient;
    private final ImmutableMap<String, Category> categoriesByCode;
    private final Map<String, Category> categoriesById;
    private final AppsFlyerTracker appsFlyerTracker;
    private final EventTracker tracker;
    private final CategoryConfiguration categoryConfiguration;

    public StatisticsGeneratorWorker(final ServiceContext serviceContext,
            FirehoseQueueProducer firehoseQueueProducer, MetricRegistry registry) {

        super(serviceContext);
        this.firehoseQueueProducer = firehoseQueueProducer;

        generateStatisticsSequenceTimer = new SequenceTimer(StatisticsGeneratorWorker.class, registry,
                SequenceTimers.GENERATE_STATISTICS);

        generateUserStateSequenceTimer = new SequenceTimer(StatisticsGeneratorWorker.class, registry,
                SequenceTimers.GENERATE_USER_STATE);

        invalidateStatisticsCacheSequenceTimer = new SequenceTimer(StatisticsGeneratorWorker.class, registry,
                SequenceTimers.INVALIDATE_STATISTICS_CACHE);

        initialCategorizationAmountLevelHistogram = registry
                .histogram(AMOUNT_PERCENTAGE_INITIAL, MetricBuckets.PERCENTAGE_BUCKETS);
        initialCategorizationCountLevelHistogram = registry
                .histogram(TRANSACTION_PERCENTAGE_INITIAL, MetricBuckets.PERCENTAGE_BUCKETS);
        initialExpensesLessThan10kCategorizationLevelHistogram = registry
                .histogram(AMOUNT_LESS_THAN_10K_PERCENTAGE_INITIAL, MetricBuckets.PERCENTAGE_BUCKETS);

        statisticsGenerationLoadUserDataTimer = registry.timer(GENERATE_USER_STATISTICS_LOAD_USER_DATA);
        statisticsGenerationLoadUserDataTransactionsTimer = registry
                .timer(GENERATE_USER_STATISTICS_LOAD_USER_DATA_TRANSACTIONS);

        refreshTimerPassword = registry.timer(REFRESH_TIMER.label("method", "password"));
        refreshTimerBankId = registry.timer(REFRESH_TIMER.label("method", "bankId"));
        refreshTimerOther = registry.timer(REFRESH_TIMER.label("method", "other"));

        tracker = serviceContext.getEventTracker();
        appsFlyerTracker = new AppsFlyerTracker();
        cacheClient = serviceContext.getCacheClient();

        accountBalanceHistoryCalculator = new AccountBalanceHistoryCalculator();

        categoriesById = Maps.uniqueIndex(categories, Category::getId);
        categoriesByCode = Maps.uniqueIndex(categories, Category::getCode);
        categoryConfiguration = serviceContext.getCategoryConfiguration();
        uncategorizedCategoryId = Iterables.find(categories, c -> (Objects
                .equal(c.getCode(), categoryConfiguration.getExpenseUnknownCode()))).getId();

        statisticsGenerator = new StatisticsGenerator(categories, categoryConfiguration, registry,
                serviceContext.getConfiguration().getCluster());
    }

    public void stop() {
        statisticsGenerator.stop();
    }

    private void calculateFavoredAccounts(UserData userData, Iterable<Transaction> nonExcludedTransactions) {
        final ImmutableMap<String, Account> accountsById = Maps.uniqueIndex(userData.getAccounts(),
                Account::getId);

        ImmutableListMultimap<String, Transaction> transactionsByAccountId = Multimaps.index(nonExcludedTransactions,
                Transaction::getAccountId);

        // Figure out the most active account.

        final List<Entry<String, Collection<Transaction>>> twoMostActiveAccounts = TRANSACTION_COLLECTION_ENTRY_SORTING_BY_COUNT
                .greatestOf(transactionsByAccountId.asMap().entrySet(), 2);

        Iterable<Account> favoredAccounts = Iterables.transform(
                twoMostActiveAccounts,
                e -> accountsById.get(e.getKey()));

        for (Account favoredAccount : favoredAccounts) {
            Account accountToFavor = accountRepository.findOne(favoredAccount.getId());
            accountToFavor.setFavored(true);
            accountRepository.save(accountToFavor);
            firehoseQueueProducer.sendAccountMessage(userData.getUser().getId(), FirehoseMessage.Type.UPDATE,
                    accountToFavor);
        }
    }

    private int calculateLevelOfCategorizedByAmount(Iterable<Transaction> transactions) {
        double amount = 0;
        double amountCategorized = 0;

        for (Transaction t : transactions) {
            amount += Math.abs(t.getAmount());

            if (!Objects.equal(t.getCategoryId(), uncategorizedCategoryId)) {
                amountCategorized += Math.abs(t.getAmount());
            }
        }

        return (int) (amountCategorized / amount * 100);
    }

    private int calculateLevelOfCategorizedExpensesLessThanByAmount(Iterable<Transaction> transactions, double max) {
        double amount = 0;
        double amountCategorized = 0;

        for (Transaction t : transactions) {
            if (!Objects.equal(t.getCategoryType(), CategoryTypes.EXPENSES) || Math.abs(t.getAmount()) >= max) {
                continue;
            }

            amount += Math.abs(t.getAmount());

            if (!Objects.equal(t.getCategoryId(), uncategorizedCategoryId)) {
                amountCategorized += Math.abs(t.getAmount());
            }
        }

        return (int) (amountCategorized / amount * 100);
    }

    private int calculateLevelOfCategorizedTransactionsByCount(Iterable<Transaction> transactions) {
        double count = 0;
        double countCategorized = 0;

        for (Transaction t : transactions) {
            count++;

            if (!Objects.equal(t.getCategoryId(), uncategorizedCategoryId)) {
                countCategorized++;
            }
        }

        return (int) (countCategorized / count * 100);
    }

    private MerchantLevelStatistics calculateLevelOfMerchantification(Iterable<Transaction> transactions) {
        double count = 0;
        double countLevelOfMerchants = 0;
        double countLevelOfMerchantsWithLocation = 0;

        for (Transaction t : transactions) {
            count++;

            // Check if we have a merchant
            if (t.getMerchantId() != null) {
                countLevelOfMerchants++;
            }

        }

        MerchantLevelStatistics result = new MerchantLevelStatistics();
        result.MerchantLevel = (int) (countLevelOfMerchants / count * 100);
        result.MerchantWithLocationLevel = (int) (countLevelOfMerchantsWithLocation / count * 100);

        return result;
    }

    private UserData loadUserData(String userId, int monthsForProcessing) {
        UserData userData = new UserData();

        userData.setUser(userRepository.findOne(userId));
        userData.setUserOrigin(userOriginRepository.findOneByUserId(userId));

        // Theory is that this stands for the majority of the time here. This timer will reject/prove this idea.
        final Context loadTransactionsTimerContext = statisticsGenerationLoadUserDataTransactionsTimer.time();

        final Date today = DateUtils.setInclusiveEndTime(new Date());
        UserProfile profile = userData.getUser().getProfile();
        int periodBreakDay = profile.getPeriodMode().equals(ResolutionTypes.MONTHLY_ADJUSTED) ? profile.getPeriodAdjustedDay() : 1;
        final DateTime startDate = DateTime.now().minusMonths(monthsForProcessing).withDayOfMonth(periodBreakDay);
        List<Transaction> collect = transactionDao.findAllByUserIdAndTime(userId, startDate,
                DateTime.now()).stream().filter(t -> t.getOriginalDate().after(startDate.toDate()))
                .collect(Collectors.toList());
        Iterable<Transaction> filter = Iterables.filter(collect,
                t -> {
                    final Date transactionDate = t.getDate();
                    return transactionDate.before(today) && EARLIEST_DATE_ALLOWED.before(transactionDate);
                });
        userData.setTransactions(Lists.newArrayList(filter));

        loadTransactionsTimerContext.stop();

        userData.setAccounts(accountRepository.findByUserId(userId));
        userData.setAccountBalanceHistory(accountBalanceHistoryRepository.findByUserId(userId));
        userData.setCredentials(credentialsRepository.findAllByUserId(userId));
        userData.setUserState(userStateRepository.findOneByUserId(userId));
        userData.setProperties(propertyRepository.findByUserId(userId));

        if (Objects.equal(Cluster.TINK, serviceContext.getConfiguration().getCluster())) {
            userData.setLoanDataByAccount(loanDataRepository.findAllByAccounts(userData.getAccounts()));
        }

        return userData;
    }

    public UserData loadUserData(final GenerateStatisticsAndActivitiesRequest request) {
        SequenceTimer.Context sequenceTimerContext = generateStatisticsSequenceTimer.time();

        // Get user data.
        sequenceTimerContext.mark(GenerateStatisticsTimers.GET_USER_DATA);

        // Load all the user's data required for statistics calculations.
        StatisticConfiguration statisticConfiguration = serviceContext.getConfiguration()
                .getStatistics();

        UserData userData = request.getUserData().orElseGet(() -> {
            // Happens for example for merchantization and fraud.

            final Context loadUserDataTimerContext = statisticsGenerationLoadUserDataTimer.time();
            try {
                return loadUserData(request.getUserId(), statisticConfiguration.monthsOfStatistics + 1);
            } finally {
                loadUserDataTimerContext.stop();
            }
        });

        sequenceTimerContext.stop();
        request.setUserData(userData);
        return userData;
    }

    // Generate statistics without any locks. You should call `ProcessService::generateStatistics` instead
    public void generateStatistics(GenerateStatisticsAndActivitiesRequest request) {
        final String userId = request.getUserId();
        final String credentialsId = request.getCredentialsId();
        UserData userData = request.getUserData().get();

        log.debug(userId, credentialsId, "Start generating statistics");

        StopWatch watch = new StopWatch();
        watch.start();
        SequenceTimer.Context sequenceTimerContext = generateStatisticsSequenceTimer.time();

        sequenceTimerContext.mark(GenerateStatisticsTimers.BALANCE_HISTORY);
        // Since we don't store calculated account balance history anymore, we always need to recalculate it.
        userData.setAccountBalanceHistory(generateAccountBalanceHistory(userData));

        // Calculate statistics.
        sequenceTimerContext.mark(GenerateStatisticsTimers.STATISTIC_TYPES);
        statisticsGenerator.populateUserDataWithStatistics(userData);
        sequenceTimerContext.stop();

        log.debug(userId, credentialsId, "Done generating.");
        int numberOfTransactions = (userData.getTransactions() != null) ? userData.getTransactions().size() : 0;
        log.info(userId, credentialsId, String.format("Generated statistics based on %d transactions in %s.",
                numberOfTransactions, watch.toString()));
    }

    public void saveCredentialsToDb(GenerateStatisticsAndActivitiesRequest request) {
        if (request.getCredentialsId() != null) {
            final Catalog catalog = Catalog.getCatalog(request.getUserData().get().getUser().getProfile().getLocale());
            updateCredentials(request.getUserData().get(), catalog, request.getCredentialsId(),
                    request.isUserTriggered());

            userStateRepository.updateContextTimestampByUserId(request.getUserId(), cacheClient);
        }
    }

    public void invalidateStatisticsCache(String userId, String credentialsId, List<String> userFlags) {
            SequenceTimer.Context sequenceTimerContext = invalidateStatisticsCacheSequenceTimer.time();
            sequenceTimerContext.mark(GenerateStatisticsTimers.INVALIDATE_CACHE);

        statisticDao.invalidateCache(userId);

            log.debug(userId, credentialsId, "Done invalidating statistics cache.");
            userStateRepository.updateStatisticsTimestampByUserId(userId, System.currentTimeMillis(), cacheClient);

            sequenceTimerContext.stop();
    }

    public void saveStatisticsToDatabase(String userId, String credentialsId, List<String> userFlags,
            List<CassandraStatistic> cassandraStatistics, StatisticContainer statisticContainer) {
        SequenceTimer.Context sequenceTimerContext = generateStatisticsSequenceTimer.time();
        sequenceTimerContext.mark(GenerateStatisticsTimers.PERSIST);
        statisticDao.save(userFlags, cassandraStatistics, statisticContainer);

        log.debug(userId, credentialsId, "Done saving statistics to database.");
        userStateRepository.updateStatisticsTimestampByUserId(userId, System.currentTimeMillis(), cacheClient);

        sequenceTimerContext.stop();
    }

    public void generateUserState(StatisticMode mode, UserData userData, Cluster cluster) {
        SequenceTimer.Context sequenceTimerContext = generateStatisticsSequenceTimer.time();

        // Generate user state for context. This is done outside of the `GENERATE` step--after `CACHE`--so that the
        // read lock can be released as soon as possible, to enable the client to fetch it.
        if (mode == StatisticMode.FULL || userData.getUserState() == null) {
            sequenceTimerContext.mark(GenerateStatisticsTimers.GENERATE_USER_STATE);
            UserState oldState = Optional.ofNullable(userData.getUserState())
                    .orElseGet(() -> userStateRepository.findOneByUserId(userData.getUser().getId()));
            UserState newState = generateUpdatedUserState(userData, cluster);
            userData.setUserState(newState);
            sendUserStateChangesToFirehose(oldState, newState);
        }

        sequenceTimerContext.stop();
    }

    public void updateContextTimestamp(String userId) {
        userStateRepository.updateContextTimestampByUserId(userId, cacheClient);
    }

    /**
     * Update user-state properties.
     *
     * @param userData the given userData object that the return value will be based upon. Uses userData.userState as
     *                 template if it exists.
     * @return a new {@link UserState} instance
     */
    private UserState generateUpdatedUserState(UserData userData, Cluster cluster) {

        SequenceTimer.Context sequenceTimerContext = generateUserStateSequenceTimer.time();
        sequenceTimerContext.mark(GenerateUserStateTimers.PREPARE);

        final String userId = userData.getUser().getId();

        log.info(userId, "Calculating user state properties");

        // Instantiate the userState we will populate.

        final UserState userState;
        {
            // If it exists on userData, use a clone of it, otherwise instantiate.

            final UserState preexistingUserState = userData.getUserState();
            if (preexistingUserState != null) {
                userState = preexistingUserState.clone();
            } else {
                userState = new UserState(userId);
            }
        }

        trackTransactionCount(userId, userData, userState, cluster);

        // debug
        log.debug(userId, "Deleting categorization suggestion cache");
        cacheClient.delete(CacheScope.SUGGEST_TRANSACTIONS_RESPONSE_BY_USERID, userId);

        UserProfile profile = userData.getUser().getProfile();
        Iterable<Transaction> nonExcludedTransactions = getNonExcludedTransactions(userData);

        if (!userState.isHaveManuallyFavoredAccount()) {
            log.debug(userId, "Calculating favorite accounts");
            sequenceTimerContext.mark(GenerateUserStateTimers.CALCULATE_FAVORED_ACCOUNTS);
            calculateFavoredAccounts(userData, nonExcludedTransactions);
        }

        // Calculate periods.
        sequenceTimerContext.mark(GenerateUserStateTimers.CALCULATE_PERIODS);
        log.debug(userId, "Calculating periods");

        PeriodCalculator periodCalculator = new PeriodCalculator(categoriesByCode, categoryConfiguration);

        userState.setPeriods(periodCalculator.calculatePeriods(profile, nonExcludedTransactions));

        // Calculate payday.
        sequenceTimerContext.mark(GenerateUserStateTimers.CALCULATE_PAYDAY);
        PaydayCalculator paydayCalculator = new PaydayCalculator(serviceContext.getCategoryConfiguration(),
                categoriesByCode, nonExcludedTransactions);
        userState.setPayday(paydayCalculator.detectPayday());
        userState.setLatestSalaryDate(paydayCalculator.detectLastSalaryDate());

        // Calculate categorization and merchantification statistics.

        // Calculate level of categorization.
        sequenceTimerContext.mark(GenerateUserStateTimers.CALCULATE_CATEGORIZATION);

        long expensesLess10kCategorizationLevel = calculateLevelOfCategorizedExpensesLessThanByAmount(
                userData.getTransactions(), 10000);

        long transactionCategorizationLevel = calculateLevelOfCategorizedTransactionsByCount(userData
                .getTransactions());

        long amountCategorizationLevel = calculateLevelOfCategorizedByAmount(userData.getTransactions());

        userState.setTransactionCategorizationLevel(transactionCategorizationLevel);
        userState.setAmountCategorizationLevel(amountCategorizationLevel);
        userState.setExpensesLessThan10kCategorizationLevel(expensesLess10kCategorizationLevel);

        // Only first time.

        if (userState.getInitialTransactionCategorizationLevel() == null) {
            userState.setInitialTransactionCategorizationLevel(transactionCategorizationLevel);
            userState.setInitialAmountCategorizationLevel(amountCategorizationLevel);
            userState.setInitialExpensesLessThan10kCategorizationLevel(expensesLess10kCategorizationLevel);

            if (transactionCategorizationLevel > 0) {
                initialCategorizationCountLevelHistogram.update(transactionCategorizationLevel);
            }
            if (amountCategorizationLevel > 0) {
                initialCategorizationAmountLevelHistogram.update(amountCategorizationLevel);
            }
            if (expensesLess10kCategorizationLevel > 0) {
                initialExpensesLessThan10kCategorizationLevelHistogram.update(expensesLess10kCategorizationLevel);
            }
        }

        // Update merchant statistics
        sequenceTimerContext.mark(GenerateUserStateTimers.CALCULATE_MERCHANTIZATION);

        MerchantLevelStatistics merchantLevel = calculateLevelOfMerchantification(userData.getTransactions());

        if (userState.getInitialMerchantificationLevel() == null) {
            userState.setInitialMerchantificationLevel(merchantLevel.MerchantLevel);
        }

        if (userState.getInitialMerchantificationWithLocationLevel() == null) {
            userState.setInitialMerchantificationWithLocationLevel(merchantLevel.MerchantWithLocationLevel);
        }

        userState.setMerchantificationLevel(merchantLevel.MerchantLevel);
        userState.setMerchantificationWithLocationLevel(merchantLevel.MerchantWithLocationLevel);

        // fetch the refresh frequency for this user
        // dont do this for now
        // userState.setUserRefreshFrequency(calcluateUserActivityFactor(statisticsGeneratorContext));

        // Extract tags.
        sequenceTimerContext.mark(GenerateUserStateTimers.EXTRACT_TAGS);
        userState.setTags(TagsUtils.extractUniqueTags(userData.getTransactions()));

        // Persist user state.
        sequenceTimerContext.mark(GenerateUserStateTimers.PERSIST);
        userStateRepository.saveAndFlush(userState);

        sequenceTimerContext.stop();

        return userState;
    }

    private void updateCredentials(final UserData userData, final Catalog catalog, final String credentialsId, final
    boolean isUserTriggered) {
        final String userId = userData.getUser().getId();

        log.info(userId, credentialsId, "Updating credentials status");

        Credentials credentials = credentialsRepository.findOne(credentialsId);
        credentials.setStatus(CredentialsStatus.UPDATED);

        if (credentials.getType() == CredentialsTypes.FRAUD) {
            credentials.setStatusPayload(Catalog.format(catalog.getString("Updated.")));
        } else {

            final Set<String> enabledAccountIds = Sets.newHashSet(Iterables.transform(
                    Iterables.filter(userData.getAccounts(),
                            a -> Objects.equal(a.getCredentialsId(), credentialsId) && !a.isExcluded()),
                    Account::getId));

            int numberOfAccounts = enabledAccountIds.size();

            int numberOfTransactions = Iterables.size(Iterables.filter(userData.getTransactions(),
                    t -> enabledAccountIds.contains(t.getAccountId())));

            credentials.setStatusPayload(Catalog.format(catalog.getString("Analyzed {0}."), CommonStringUtils
                    .formatCredentialsStatusPayloadSuffix(numberOfAccounts, numberOfTransactions, catalog)));
        }

        measureRefreshTime(credentials);

        UpdateCredentialsStatusRequest updateCredentialsRequest = new UpdateCredentialsStatusRequest();
        updateCredentialsRequest.setCredentials(credentials);
        updateCredentialsRequest.setUserId(userId);
        updateCredentialsRequest.setManual(isUserTriggered);

        serviceContext.getSystemServiceFactory().getUpdateService().updateCredentials(updateCredentialsRequest);
    }

    private void measureRefreshTime(Credentials credentials) {
        // Current time before accessing cache, to not include that time in metric.
        long currentTimestamp = System.currentTimeMillis();
        Object startTimestampObject = cacheClient
                .get(CacheScope.FULL_REFRESH_TIMER_BY_CREDENTIALS, credentials.getId());
        if (startTimestampObject != null) {
            long startTimestamp = (long) startTimestampObject;
            long duration = currentTimestamp - startTimestamp;

            if (startTimestamp > 0 && duration < UpdateCredentialsStatusRequest.MAX_REFRESH_TIME) {
                if (credentials.getType().equals(CredentialsTypes.PASSWORD)) {
                    refreshTimerPassword.update(duration, TimeUnit.MILLISECONDS);
                } else if (credentials.getType().equals(CredentialsTypes.MOBILE_BANKID)) {
                    refreshTimerBankId.update(duration, TimeUnit.MILLISECONDS);
                } else {
                    refreshTimerOther.update(duration, TimeUnit.MILLISECONDS);
                }
            } else if (duration < 0) {
                log.warn(
                        credentials,
                        String.format(
                                "Clocks are off when measuring round trip time for refresh. currentTimestamp:%s startTimestamp:%s",
                                currentTimestamp, startTimestamp));
            }
        }
    }

    /**
     * Generate an updated list of account history given populated user data.
     *
     * @param userData the user's data. The datastructure remains unmodified throughout this execution.
     * @return an new list of account history objects.
     */
    private List<AccountBalance> generateAccountBalanceHistory(UserData userData) {

        ImmutableListMultimap<UUID, AccountBalance> accountBalanceHistoryByAccountId = Multimaps.index(
                userData.getAccountBalanceHistory(), AccountBalance::getAccountId);

        ImmutableListMultimap<String, Transaction> transactionsByAccountId = Multimaps.index(
                userData.getTransactions(), Transaction::getAccountId);

        ImmutableMap<String, Credentials> credentialsById = Maps.uniqueIndex(
                userData.getCredentials(), Credentials::getId);

        List<AccountBalance> accountBalanceHistory = Lists.newArrayList();

        for (Account account : userData.getAccounts()) {
            if (account.isExcluded()) {
                continue;
            }

            Credentials credentials = credentialsById.get(account.getCredentialsId());

            if (credentials == null) {
                continue;
            }

            accountBalanceHistory.addAll(accountBalanceHistoryCalculator.calculate(
                    account,
                    transactionsByAccountId.get(account.getId()),
                    accountBalanceHistoryByAccountId.get(UUIDUtils.fromTinkUUID(account.getId())),
                    Objects.equal(credentials.getStatus(), CredentialsStatus.DISABLED)));
        }

        return accountBalanceHistory;
    }

    private void sendUserStateChangesToFirehose(UserState oldUserState, UserState newUserState) {
        sendPeriodChangesToFirehose(oldUserState.getUserId(), oldUserState.getPeriods(), newUserState.getPeriods());
    }

    private void sendPeriodChangesToFirehose(String userId, List<Period> oldPeriods, List<Period> newPeriods) {
        Map<String, Period> oldPeriodsMap = oldPeriods.stream()
                .collect(Collectors.toMap(Period::getName, period -> period));
        Map<String, Period> newPeriodsMap = newPeriods.stream()
                .collect(Collectors.toMap(Period::getName, period -> period));

        List<Period> addedPeriods = newPeriods.stream()
                .filter(period -> !oldPeriodsMap.containsKey(period.getName()))
                .collect(Collectors.toList());
        List<Period> removedPeriods = oldPeriods.stream()
                .filter(period -> !newPeriodsMap.containsKey(period.getName()))
                .collect(Collectors.toList());
        List<Period> updatedPeriods = newPeriods.stream()
                .filter(period -> oldPeriodsMap.containsKey(period.getName()) && !period
                        .equals(oldPeriodsMap.get(period.getName())))
                .collect(Collectors.toList());

        if (!addedPeriods.isEmpty()) {
            firehoseQueueProducer.sendPeriodsMessage(userId, FirehoseMessage.Type.CREATE, addedPeriods);
        }

        if (!removedPeriods.isEmpty()) {
            firehoseQueueProducer.sendPeriodsMessage(userId, FirehoseMessage.Type.DELETE, removedPeriods);
        }

        if (!updatedPeriods.isEmpty()) {
            firehoseQueueProducer.sendPeriodsMessage(userId, FirehoseMessage.Type.UPDATE, updatedPeriods);
        }
    }

    private void trackTransactionCount(String userId, UserData userData, UserState userState, Cluster cluster) {

        // Don't track anything for user with only test credentials.
        if (isDemoUser(userData)) {
            return;
        }

        // Don't track if tracking is disabled
        if (isTrackingDisabled(userData)) {
            return;
        }

        if (Objects.equal(cluster, Cluster.ABNAMRO)) {
            trackTransactionCountForAbnAmro(userId, userData, userState);
        } else {
            trackTransactionCountForTink(userId, userData, userState);
        }
    }

    private void trackTransactionCountForAbnAmro(String userId, UserData userData, UserState userState) {

        if (!userState.isHaveHadTransactions() && !userData.getTransactions().isEmpty()) {
            userState.setHaveHadTransactions(true);
            tracker.trackEvent(
                    TrackableEvent.event(userId, "system.have-transactions", Maps.newHashMap()));
        }

        Map<String, Object> userProperties = Maps.newHashMap();
        userProperties.put("Number of transactions", Iterables.size(userData.getTransactions()));
        tracker.trackUserProperties(TrackableEvent.userProperties(userId, userProperties));
    }

    private void trackTransactionCountForTink(String userId, UserData userData, UserState userState) {

        UserOrigin origin = userData.getUserOrigin();

        if (!userState.isHaveHadTransactions() && !userData.getTransactions().isEmpty()) {

            userState.setHaveHadTransactions(true);

            Map<String, Object> properties = Maps.newHashMap();
            properties.put("Market", userData.getUser().getProfile().getMarket());

            tracker.trackEvent(TrackableEvent.event(userId, "system.have-transactions", properties));
            tracker.trackEvent(TrackableEvent.event(userId, "system.have-transactions-or-fraud", properties));

            if (origin != null && UserOrigin.SERVICE_NAME_APPSFLYER.equals(origin.getServiceName())) {

                String deviceType = origin.getDeviceType();

                if ("ios".equals(deviceType) || "android".equals(deviceType)) {
                    appsFlyerTracker.trackEvent(AppsFlyerEventBuilder.client(deviceType, origin.getExternalServiceId())
                            .haveTransactions().build());
                }
            }
        }

        Map<String, Object> userProperties = Maps.newHashMap();
        userProperties.put("Number of transactions", Iterables.size(userData.getTransactions()));
        tracker.trackUserProperties(TrackableEvent.userProperties(userId, userProperties));
    }

    private static boolean isDemoUser(UserData userData) {
        if (userData == null || userData.getUser() == null) {
            return false;
        }
        return userData.getUser().getFlags().contains(FeatureFlags.DEMO_USER_ON);
    }

    private static boolean isTrackingDisabled(UserData userData) {
        if (userData == null || userData.getUser() == null) {
            return false;
        }
        return !userData.getUser().isTrackingEnabled();
    }
}
