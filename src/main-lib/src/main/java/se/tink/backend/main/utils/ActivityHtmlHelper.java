package se.tink.backend.main.utils;

import com.codahale.metrics.annotation.Timed;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.NoResultException;
import se.tink.backend.abnamro.workers.activity.renderers.AbnAmroAutomaticSavingsSummaryActivityRenderer;
import se.tink.backend.abnamro.workers.activity.renderers.AbnAmroMaintenanceActivityRenderer;
import se.tink.backend.abnamro.workers.activity.renderers.AbnAmroMonthlySummaryActivityRenderer;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.Versions;
import se.tink.backend.common.dao.ActivityDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.template.PooledRythmProxy;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.TemplateUtils;
import se.tink.backend.common.workers.activity.renderers.AccountBalanceActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.ActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.ActivityRendererContext;
import se.tink.backend.common.workers.activity.renderers.ApplicationActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.BadgeActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.BankFeeSelfieActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.DiscoverActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.EInvoicesActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.EmptyActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.FollowActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.FraudActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.HeatMapActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.LeftToSpendActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.LoanEventActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.LookbackActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.MonthlySummaryActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.RateThisAppActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.ReimbursementActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.SuggestProviderActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.TransactionRenderer;
import se.tink.backend.common.workers.activity.renderers.UnusualActivityActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.WeeklySummaryActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.svg.charts.SuggestCategoryActivityRenderer;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Currency;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.User;
import se.tink.backend.core.UserState;
import se.tink.backend.rpc.HtmlDetailsResponse;
import se.tink.backend.rpc.HtmlHeadResponse;
import se.tink.backend.rpc.ListActivityHtmlCommand;
import se.tink.backend.rpc.ListHtmlResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MeterFactory;

public class ActivityHtmlHelper {
    private static final LogUtils log = new LogUtils(ActivityHtmlHelper.class);
    private final Histogram htmlSizeHistogram;
    private final ActivityDao activityDao;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CredentialsRepository credentialsRepository;
    private final UserStateRepository userStateRepository;
    private final CategoryConfiguration categoryConfiguration;
    private final ImmutableMap<String, Currency> currenciesByCode;
    private final PooledRythmProxy templateRenderer;
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;
    private final Cluster cluster;

    @Inject
    public ActivityHtmlHelper(ActivityDao activityDao,
            AccountRepository accountRepository, CategoryRepository categoryRepository,
            CredentialsRepository credentialsRepository, UserStateRepository userStateRepository,
            CategoryConfiguration categoryConfiguration, ImmutableMap<String, Currency> currenciesByCode,
            PooledRythmProxy templateRenderer, MeterFactory meterFactory,
            DeepLinkBuilderFactory deepLinkBuilderFactory, Cluster cluster) {
        this.activityDao = activityDao;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.credentialsRepository = credentialsRepository;
        this.userStateRepository = userStateRepository;
        this.categoryConfiguration = categoryConfiguration;
        this.currenciesByCode = currenciesByCode;
        this.templateRenderer = templateRenderer;
        this.htmlSizeHistogram = meterFactory.getHistogram("html_size_histogram");
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
        this.cluster = cluster;
    }

    public ListHtmlResponse listHtml(User user, ListActivityHtmlCommand command) {
        return listHtml(user,
                command.getActivityList(),
                command.getUserAgent(),
                command.getOffset(),
                command.getLimit(),
                command.getScreenWidthl(),
                command.getScreenPpi());
    }

    @Timed
    public ListHtmlResponse listHtml(User user, List<Activity> activityList, String userAgent, int offset, int limit,
            double screenWidth, double screenPpi) {
        if (screenWidth == 0) {
            screenWidth = 480;
        }
        if (screenPpi == 0) {
            screenPpi = 160;
        }

        ActivityRendererContext context = buildActivityRendererContext(user, userAgent, screenWidth, screenPpi,
                cluster);
        String userId = user.getId();

        List<String> activityKeyList = Lists.newArrayList();
        List<String> activityIdentifiersList = Lists.newArrayList();

        StringBuilder builder = new StringBuilder();

        builder.append("<div class=\"page\">");

        ActivityRenderer tr = new TransactionRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer ar = new AccountBalanceActivityRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer l2sr = new LeftToSpendActivityRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer fr = new FollowActivityRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer uar = new UnusualActivityActivityRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer suggestRenderer = new SuggestCategoryActivityRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer badgeRenderer = new BadgeActivityRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer merchantMapRenderer = new HeatMapActivityRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer weeklySummaryActivityRenderer = new WeeklySummaryActivityRenderer(context,
                deepLinkBuilderFactory);
        ActivityRenderer monthlySummaryActivityRenderer = new MonthlySummaryActivityRenderer(context,
                deepLinkBuilderFactory);
        ActivityRenderer fraudActivityRenderer = new FraudActivityRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer suggestProviderActivityRenderer = new SuggestProviderActivityRenderer(context,
                deepLinkBuilderFactory);
        ActivityRenderer discoverActivityRenderer = new DiscoverActivityRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer emptyActivityRenderer = new EmptyActivityRenderer(context);
        ActivityRenderer eInvoicesRenderer = new EInvoicesActivityRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer applicationRenderer = new ApplicationActivityRenderer(context, deepLinkBuilderFactory);
        ActivityRenderer rateThisAppRenderer = new RateThisAppActivityRenderer(context, userStateRepository);
        ActivityRenderer loanEventRenderer = new LoanEventActivityRenderer(context, deepLinkBuilderFactory);

        ActivityRenderer abnAmroMonthlySummaryActivityRenderer = new AbnAmroMonthlySummaryActivityRenderer(context,
                deepLinkBuilderFactory);
        ActivityRenderer abnAmroAutomaticSavingsSummaryActivityRenderer = new AbnAmroAutomaticSavingsSummaryActivityRenderer(
                context, deepLinkBuilderFactory);
        ActivityRenderer abnAmroMaintenanceActivityRenderer = new AbnAmroMaintenanceActivityRenderer(context);

        ActivityRenderer reimbursementRenderer = new ReimbursementActivityRenderer(context, deepLinkBuilderFactory);

        for (Activity activity : activityList) {
            if (hasShareableContent(activity)) {
                try {
                    activityDao.saveShared(userId, activity);
                } catch (NoResultException ignored) {
                }
            }

            String s = null;

            try {
                switch (activity.getType()) {
                    case Activity.Types.MONTHLY_SUMMARY_ABNAMRO:
                        s = abnAmroMonthlySummaryActivityRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.MONTHLY_SUMMARY:
                        s = monthlySummaryActivityRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.WEEKLY_SUMMARY:
                        s = weeklySummaryActivityRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.FRAUD:
                        s = fraudActivityRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.SUGGEST_MERCHANTS:
                        // Disable suggest merchants rendering
                        break;
                    case Activity.Types.MERCHANT_HEAT_MAP:
                        s = merchantMapRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.BADGE:
                        if (Versions.shouldUseNewFeed(TinkUserAgent.of(Optional.of(userAgent)), cluster)) {
                            continue;
                        } else {
                            s = badgeRenderer.renderHtml(activity);
                        }
                        break;
                    case Activity.Types.SUGGEST:
                        s = suggestRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.UNUSUAL_ACCOUNT:
                    case Activity.Types.UNUSUAL_CATEGORY_HIGH:
                    case Activity.Types.UNUSUAL_CATEGORY_LOW:
                        s = uar.renderHtml(activity);
                        break;
                    case Activity.Types.FOLLOW_EXPENSES:
                    case Activity.Types.FOLLOW_SEARCH:
                        s = fr.renderHtml(activity);
                        break;
                    case Activity.Types.LEFT_TO_SPEND:
                        s = l2sr.renderHtml(activity);
                        break;
                    case Activity.Types.TRANSFER:
                    case Activity.Types.TRANSACTION:
                    case Activity.Types.TRANSACTION_MULTIPLE:
                    case Activity.Types.INCOME_MULTIPLE:
                    case Activity.Types.INCOME:
                    case Activity.Types.DOUBLE_CHARGE:
                    case Activity.Types.LARGE_EXPENSE:
                    case Activity.Types.LARGE_EXPENSE_MULTIPLE:
                    case Activity.Types.BANK_FEE:
                    case Activity.Types.BANK_FEE_MULTIPLE:
                        s = tr.renderHtml(activity);
                        break;
                    case Activity.Types.BALANCE_LOW:
                    case Activity.Types.BALANCE_HIGH:
                        s = ar.renderHtml(activity);
                        break;
                    case Activity.Types.SUGGEST_PROVIDER:
                        s = suggestProviderActivityRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.BANK_SELFIE:
                        // Disable the rendering of bank fee selfie
                        break;
                    case Activity.Types.DISCOVER_BUDGETS:
                    case Activity.Types.DISCOVER_CATEGORIES:
                        s = discoverActivityRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.DISCOVER_EMPTY:
                        s = emptyActivityRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.EINVOICES:
                        s = eInvoicesRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.APPLICATION_SAVINGS:
                    case Activity.Types.APPLICATION_RESUME_SAVINGS:
                        if (!TinkUserAgent.of(Optional.ofNullable(userAgent)).hasValidVersion(Versions.Ios.NewFeed, Versions.Android.NewFeed)) {
                            s = applicationRenderer.renderHtml(activity);
                        }
                        break;
                    case Activity.Types.APPLICATION_MORTGAGE:
                    case Activity.Types.APPLICATION_RESUME_MORTGAGE:
                        s = applicationRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.AUTOMATIC_SAVINGS_SUMMARY_ABNAMRO:
                        s = abnAmroAutomaticSavingsSummaryActivityRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.MAINTENANCE_INFORMATION_ABNAMRO:
                        s = abnAmroMaintenanceActivityRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.YEAR_IN_NUMBERS:
                        // Disable the year in numbers rendering
                        break;
                    case Activity.Types.REIMBURSEMENT:
                        s = reimbursementRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.SUMMER_IN_NUMBERS:
                        // Disable the summer in numbers rendering
                        break;
                    case Activity.Types.RATE_THIS_APP:
                        s = rateThisAppRenderer.renderHtml(activity);
                        break;
                    case Activity.Types.LOAN:
                    case Activity.Types.LOAN_DECREASE:
                    case Activity.Types.LOAN_INCREASE:
                        s = loanEventRenderer.renderHtml(activity);
                        break;
                    default:
                        s = null;
                }
            } catch (Exception e) {
                log.error(user.getId(), "Could not render activity", e);
            }

            if (!Strings.isNullOrEmpty(s)) {
                if (activity.getKey() != null) {
                    activityKeyList.add(activity.getKey());
                    activityIdentifiersList.add(activity.getFeedActivityIdentifier());

                    builder.append(s);
                } else {
                    log.warn(context.getUser().getId(),
                            String.format("Could not add activity to feed, key:%s, identifier:%s",
                                    activity.getKey(), activity.getFeedActivityIdentifier()));
                }
            }
        }

        builder.append("</div>");

        ListHtmlResponse response = new ListHtmlResponse();
        response.setHtmlPage(builder.toString());
        response.setNextPageOffset(offset + limit);
        response.setActivityKeys(activityKeyList);
        response.setFeedActivityIdentifiersList(activityIdentifiersList);

        htmlSizeHistogram.update(response.getHtmlPage().length());

        return response;
    }

    private ActivityRendererContext buildActivityRendererContext(User user, String userAgent, double screenWidth,
            double screenPpi, Cluster cluster) {

        List<Category> cs = categoryRepository.findAll(user.getProfile().getLocale());
        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());
        List<Account> accounts = accountRepository.findByUserId(user.getId());
        ActivityRendererContext context = new ActivityRendererContext();
        UserState userState = userStateRepository.findOneByUserId(user.getId());
        List<Period> cleanPeriods = (userState != null) ? DateUtils.getCleanPeriods(userState.getPeriods())
                : Lists.newArrayList();

        TinkUserAgent tinkUserAgent = new TinkUserAgent(userAgent);

        context.setCleanPeriods(cleanPeriods);
        context.setCategories(cs);
        context.setCategoryConfiguration(categoryConfiguration);
        context.setCredentials(credentials);
        context.setAccounts(accounts);
        context.setUser(user);
        context.setCatalog(Catalog.getCatalog(user.getProfile().getLocale()));
        context.setCurrencies(currenciesByCode);
        context.setScreenWidth(screenWidth);
        context.setScreenResolution(screenPpi);
        context.setTheme(Theme.getTheme(cluster, tinkUserAgent));
        context.setTemplateRenderer(templateRenderer);
        context.setUserAgent(tinkUserAgent);
        context.setCluster(cluster);

        return context;
    }

    private boolean hasShareableContent(Activity a) {
        switch (a.getType()) {
        case Activity.Types.BADGE:
        case Activity.Types.MERCHANT_HEAT_MAP:
        case Activity.Types.LOOKBACK:
        case Activity.Types.BANK_SELFIE:
            return true;
        default:
            return false;
        }
    }

    public HtmlDetailsResponse activityDetails(User user, String userAgent, String activityId, double screenWidth,
            double screenPpi) throws NoSuchElementException {
        Activity activity = activityDao.findShared(user.getId(), activityId);
        if (activity == null) {
            throw new NoSuchElementException();
        }

        ActivityRendererContext context = buildActivityRendererContext(user, userAgent, screenWidth, screenPpi,
                cluster);

        BadgeActivityRenderer badgeRenderer = new BadgeActivityRenderer(context, deepLinkBuilderFactory);
        LookbackActivityRenderer lookbackRenderer = new LookbackActivityRenderer(context, deepLinkBuilderFactory);
        HeatMapActivityRenderer merchantMapRenderer = new HeatMapActivityRenderer(context, deepLinkBuilderFactory);
        BankFeeSelfieActivityRenderer bankFeeSelfieActivityRenderer = new BankFeeSelfieActivityRenderer(context,
                deepLinkBuilderFactory);

        HtmlDetailsResponse response;
        switch (activity.getType()) {
        case Activity.Types.BADGE:
            response = badgeRenderer.renderDetailsHtml(activity);
            break;
        case Activity.Types.MERCHANT_HEAT_MAP:
            response = merchantMapRenderer.renderDetailsHtml(activity);
            break;
        case Activity.Types.LOOKBACK:
            response = lookbackRenderer.renderDetailsHtml(activity);
            break;
        case Activity.Types.BANK_SELFIE:
            response = bankFeeSelfieActivityRenderer.renderDetailsHtml(activity);
            break;
        default:
            response = null;
        }
        return response;
    }

    @Timed
    public HtmlHeadResponse htmlHead(User user, String userAgent) {
        HtmlHeadResponse response = new HtmlHeadResponse();
        response.setScripts(TemplateUtils.getTemplate("data/templates/activities/scripts.html"));
        response.setCss(TemplateUtils.getTemplate(
                String.format("%s.css", getTheme(new TinkUserAgent(userAgent)))));

        return response;
    }

    private String getTheme(TinkUserAgent userAgent) {
        if (Objects.equals(cluster, Cluster.ABNAMRO)) {
            if (userAgent.isIOS()) {
                if (Versions.shouldUseNewFeed(userAgent, cluster)) {
                    return "abnamro-ios-v2";
                }
                return "abnamro-ios";
            } else if (userAgent.isAndroid()) {
                if (Versions.shouldUseNewFeed(userAgent, cluster)) {
                    return "abnamro-android-v2";
                }
                return "abnamro-android";
            } else {
                return "abnamro";
            }
        } else {
            if (userAgent.isIOS()) {
                if (Versions.shouldUseNewFeed(userAgent, cluster)) {
                    return "tink-ios-v2";
                }
                return "tink-ios";
            } else if (userAgent.isAndroid()) {
                if (Versions.shouldUseNewFeed(userAgent, cluster)) {
                    return "tink-android-v2";
                }
                return "tink-android";
            } else {
                return "tink";
            }
         }
    }
}
