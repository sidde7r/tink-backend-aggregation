package se.tink.backend.common.mail.monthly.summary;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.Months;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.dao.ActivityDao;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.mail.SubscriptionHelper;
import se.tink.backend.common.mail.monthly.summary.calculators.ActivityDataCalculator;
import se.tink.backend.common.mail.monthly.summary.calculators.BudgetCalculator;
import se.tink.backend.common.mail.monthly.summary.calculators.CategoryDataCalculator;
import se.tink.backend.common.mail.monthly.summary.calculators.FraudDataCalculator;
import se.tink.backend.common.mail.monthly.summary.model.ActivityData;
import se.tink.backend.common.mail.monthly.summary.model.BudgetData;
import se.tink.backend.common.mail.monthly.summary.model.CategoryData;
import se.tink.backend.common.mail.monthly.summary.model.EmailContent;
import se.tink.backend.common.mail.monthly.summary.model.EmailResult;
import se.tink.backend.common.mail.monthly.summary.model.FraudData;
import se.tink.backend.common.mail.monthly.summary.renderers.MonthlyEmailHtmlRendererV2;
import se.tink.backend.common.mail.monthly.summary.utils.PeriodUtils;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.FraudItemRepository;
import se.tink.backend.common.template.PooledRythmProxy;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.follow.FollowItem;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;
import static se.tink.backend.common.mail.monthly.summary.utils.Filters.transactionIsWithinPeriod;

public class MonthlySummaryGenerator {

    private final AccountRepository accountRepository;
    private final ActivityDao activityDao;
    private final FollowItemRepository followItemRepository;
    private final CategoryRepository categoryRepository;
    private final StatisticDao statisticDao;
    private final TransactionDao transactionDao;
    private final FraudDetailsRepository fraudDetailsRepository;
    private final FraudItemRepository fraudItemRepository;
    private final SubscriptionHelper subscriptionHelper;
    private final CategoryConfiguration categoryConfiguration;
    private final PooledRythmProxy pooledRythmProxy;
    private final ElasticSearchClient elasticSearchClient;
    @Inject
    public MonthlySummaryGenerator(TransactionDao transactionDao,
            AccountRepository accountRepository,
            StatisticDao statisticDao,
            ActivityDao activityDao,
            FollowItemRepository followItemRepository,
            CategoryRepository categoryRepository,
            FraudDetailsRepository fraudDetailsRepository,
            FraudItemRepository fraudItemRepository,
            SubscriptionHelper subscriptionHelper,
            CategoryConfiguration categoryConfiguration,
            PooledRythmProxy pooledRythmProxy,
            ElasticSearchClient elasticSearchClient) {
        this.transactionDao = transactionDao;
        this.accountRepository = accountRepository;
        this.statisticDao = statisticDao;
        this.activityDao = activityDao;
        this.followItemRepository = followItemRepository;
        this.categoryRepository = categoryRepository;
        this.fraudDetailsRepository = fraudDetailsRepository;
        this.fraudItemRepository = fraudItemRepository;
        this.subscriptionHelper = subscriptionHelper;
        this.categoryConfiguration = categoryConfiguration;
        this.pooledRythmProxy = pooledRythmProxy;
        this.elasticSearchClient = elasticSearchClient;
    }

    /**
     * Generates an email comparing previous month with the month before (will be send on the first day in the
     * new month)
     */
    public EmailResult generateEmail(User user) {
        return generateEmail(user, -1, -2);
    }

    private EmailResult generateEmail(User user, int firstMonthOffset, int secondMonthOffset) {
        Preconditions.checkArgument(firstMonthOffset <= 0);
        Preconditions.checkArgument(secondMonthOffset <= 0);
        Preconditions.checkArgument(secondMonthOffset < firstMonthOffset);

        EmailResult email = new EmailResult();

        email.setSubject(getMailSubject(user, firstMonthOffset));
        email.setContent(getMailContent(user, firstMonthOffset, secondMonthOffset));

        return email;
    }

    private String getMailContent(final User user, int firstMonthOffset, int secondMonthOffset) {
        final ResolutionTypes resolution = user.getProfile().getPeriodMode();
        final int periodBreakDate = user.getProfile().getPeriodAdjustedDay();

        PeriodUtils periodUtils = new PeriodUtils(user.getProfile());

        final String firstMonthPeriod = periodUtils.getMonthPeriodFromToday(Months.months(firstMonthOffset));
        final String secondMonthPeriod = periodUtils.getMonthPeriodFromToday(Months.months(secondMonthOffset));

        // Verify that we have transactions for the first period
        FluentIterable<Transaction> transactions = FluentIterable
                .from(transactionDao.findAllByUserId(user.getId()))
                .filter(transactionIsWithinPeriod(firstMonthPeriod, user.getProfile()));

        if (transactions.size() == 0) {
            return null;
        }

        final List<Category> categories = categoryRepository.findAll(user.getProfile().getLocale());

        // Verify that we have categorized transactions for the first period
        final ImmutableMap<String, Category> categoriesById = Maps.uniqueIndex(categories,
                c -> (c.getId()));

        int categorizedTransactionCount = transactions.filter(
                t -> (!Objects.equal(categoriesById.get(t.getCategoryId()).getCode(),
                        categoryConfiguration.getExpenseUnknownCode()
                ))).size();

        if (categorizedTransactionCount == 0) {
            return null;
        }

        List<Statistic> allStatistics = statisticDao.findByUserId(user.getId());
        final List<Account> accounts = accountRepository.findByUserId(user.getId());
        List<Statistic> statistics = allStatistics.stream()
                .filter(cs -> cs.getType().equals(Statistic.Types.EXPENSES_BY_CATEGORY)).collect(Collectors.toList());


        Date firstPeriodEndDate = DateUtils.getLastDateFromPeriod(firstMonthPeriod, resolution, periodBreakDate);

        List<FollowItem> followItems = followItemRepository.findByUserId(user.getId());
        
        // Create budget data
        BudgetCalculator budgetCalculator = new BudgetCalculator(categoryConfiguration);
        List<BudgetData> budgetData = budgetCalculator
                .getBudgetData(followItems, firstPeriodEndDate, user, elasticSearchClient.getTransactionsSearcher(),
                        firstMonthPeriod, transactions, accounts, allStatistics, categories);

        // Create category data
        CategoryDataCalculator categoryDataCalculator = new CategoryDataCalculator();
        categoryDataCalculator.setCategories(categories);
        categoryDataCalculator.setFirstPeriodStatistics(statistics.stream().filter(s -> Objects.equal(s.getPeriod(), firstMonthPeriod)).collect(
                Collectors.toList()));
        categoryDataCalculator.setSecondPeriodStatistics(statistics.stream().filter(s -> Objects.equal(s.getPeriod(), secondMonthPeriod)).collect(
                Collectors.toList()));

        List<CategoryData> categoryData = categoryDataCalculator.getCategoryData();

        // Create activity data
        FluentIterable<Activity> activities = FluentIterable.from(activityDao.findByUserId(user.getId()));

        ActivityData activityData = new ActivityDataCalculator(activities)
                .getActivityData(firstMonthPeriod, resolution, periodBreakDate);

        // Create fraud data
        FraudDataCalculator fraudCalculator = new FraudDataCalculator(user,
                fraudDetailsRepository.findAllByUserId(user.getId()),
                fraudItemRepository.findAllByUserId(user.getId()));

        FraudData fraudData = fraudCalculator.getFraudDataForPeriod(resolution, periodBreakDate, firstMonthPeriod);

        // Render the HTML.
        MonthlyEmailHtmlRendererV2 htmlGenerator = new MonthlyEmailHtmlRendererV2();

        EmailContent emailContent = new EmailContent();
        emailContent.setTitle(getMailSubject(user, firstMonthOffset));
        emailContent.setStartDate(UserProfile.ProfileDateUtils.getFirstDateFromPeriod(firstMonthPeriod, user.getProfile()));
        emailContent.setEndDate(UserProfile.ProfileDateUtils.getLastDateFromPeriod(firstMonthPeriod, user.getProfile()));
        emailContent.setLocale(user.getProfile().getLocale());
        emailContent.setNumberOfCategorizedTransactions(categorizedTransactionCount);
        emailContent.setUserId(user.getId());
        emailContent.setUnsubscribeToken(subscriptionHelper.getOrCreateTokenFor(user.getId()));
        emailContent.setPeriodMode(user.getProfile().getPeriodMode());

        emailContent.setBudgetData(budgetData);
        emailContent.setActivityData(activityData);
        emailContent.setFraudData(fraudData);
        emailContent.setCategoryData(categoryData);

        return htmlGenerator.renderEmail(pooledRythmProxy, emailContent);
    }

    private String getMailSubject(User user, int monthOffset) {
        PeriodUtils periodUtils = new PeriodUtils(user.getProfile());

        String period = periodUtils.getMonthPeriodFromToday(Months.months(monthOffset));

        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        SimpleDateFormat monthFormat = new SimpleDateFormat(catalog.getString("MMMMM yyyy"),
                Catalog.getLocale(user.getProfile().getLocale()));

        return StringUtils.firstLetterUppercaseFormatting(monthFormat.format(DateTime.parse(period).toDate()));
    }

}
