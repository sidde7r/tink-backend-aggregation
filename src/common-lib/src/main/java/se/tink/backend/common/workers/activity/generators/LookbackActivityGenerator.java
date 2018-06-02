package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import org.apache.commons.lang.StringEscapeUtils;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.utils.TemplateUtils;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.HtmlActivityIconData;
import se.tink.backend.common.workers.activity.generators.models.ShareableDetailsHtmlActivityData;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.LookbackActivityContent;
import se.tink.backend.core.Notification;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class LookbackActivityGenerator extends CustomHtmlActivtyGenerator {

    private static final LogUtils log = new LogUtils(LookbackActivityGenerator.class);

    private static class CategoryLookbackInformation {
        private static final Random RANDOM = new Random();

        private static String getRandomString(List<String> descriptions) {
            synchronized (RANDOM) {
                return descriptions.get(RANDOM.nextInt(descriptions.size()));
            }
        }

        private final List<String> images;
        private final String questionAmount;
        private final String questionCount;
        private final String statementAmount;
        private final String statementCount;
        private final String titleCount;

        public CategoryLookbackInformation(String questionAmount, String questionCount, List<String> images,
                String statementAmount, String titleCount, String statementCount) {
            this.questionAmount = questionAmount;
            this.questionCount = questionCount;
            this.images = images;
            this.statementAmount = statementAmount;
            this.titleCount = titleCount;
            this.statementCount = statementCount;
        }

        public String getImage() {
            return getRandomString(images);
        }

        public String getQuestionAmount() {
            return questionAmount;
        }

        public String getQuestionCount() {
            return questionCount;
        }

        public String getStatementAmount() {
            return statementAmount;
        }

        public String getStatementCount() {
            return statementCount;
        }

        public String getTitleCount() {
            return titleCount;
        }
    }

    public static final Ordering<Activity> ACTIVITIES_ORDERING = new Ordering<Activity>() {
        @Override
        public int compare(Activity left, Activity right) {
            return ComparisonChain.start().compare(left.getDate(), right.getDate()).result();
        }
    };

    private static final long MIN_ACTIVITY_PERIODICITY = 2 * 7 * 24 * 60 * 60 * 1000;

    protected static final Ordering<Statistic> STATISTICS_ORDERING = new Ordering<Statistic>() {
        @Override
        public int compare(Statistic left, Statistic right) {
            return ComparisonChain.start().compare(Math.abs(left.getValue()), Math.abs(right.getValue())).result();
        }
    };

    protected static final Ordering<Transaction> TRANSACTION_ORDERING = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction left, Transaction right) {
            return ComparisonChain.start().compare(Math.abs(left.getAmount()), Math.abs(right.getAmount())).result();
        }
    };

    private static Map<String, CategoryLookbackInformation> constructCategoryInformation(Catalog catalog,
            CategoryConfiguration categoryConfiguration) {
        Map<String, CategoryLookbackInformation> categoryInformationByCategoryCode = Maps.newHashMap();

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getRestaurantsCode(),
                new CategoryLookbackInformation("Minns vilken bra middag du hade den här dagen {0}?",
                        "Kommer du ihåg den här dagen {0} då du verkar ha ätit på alla restauranger i stan?", Lists
                        .newArrayList("cover_food.jpg", "cover_restaurants_1.jpg", "cover_restaurants_2.jpg",
                                "cover_restaurants_3.jpg"),
                        "Den {0} hade du din största restaurangnota någonsin {1}", "{0} notor på en dag",
                        "Den här var dagen du hade flest restaurangköp {0}"));

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getTaxiCode(),
                new CategoryLookbackInformation("Kommer du ihåg den riktigt långa taxiresan {0}?",
                        "Kommer du ihåg den här dagen {0} när du åkte taxi hela dagen?", Lists.newArrayList(
                        "cover_taxi_1.jpg", "cover_taxi_2.jpg"),
                        "Den {0} hade du din största taxiutgift någonsin {1}", "{0} taxiturer på en dag",
                        "Den här var dagen du hade flest taxiköp {0}"));

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getCoffeeCode(),
                new CategoryLookbackInformation("Kommer du ihåg {0} när du fikade bort den här dagen?",
                        "Minns du den här dagen {0} när du slog fikarekord?", Lists.newArrayList("cover_coffee_1.jpg",
                        "cover_coffee_2.jpg", "cover_coffee_3.jpg"),
                        "Den {0} hade du din största fikautgift på hela året {1}", "{0} fikaköp på en dag",
                        "Den här var dagen du hade flest fikaköp {0}"));

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getBarsCode(),
                new CategoryLookbackInformation("Vi hoppas du kommer ihåg barrundan den här kvällen {0}?",
                        "Kommer du ihåg den här kvällen {0} när du bjöd alla dina vänner på drinkar?", Lists
                        .newArrayList("cover_bar_1.jpg", "cover_bar_2.jpg", "cover_bar_3.jpg"),
                        "Den {0} hade du din största barnota på hela året {1}", "{0} barköp på en kväll",
                        "Den här var kvällen du hade flest barköp {0}"));

        categoryInformationByCategoryCode
                .put(categoryConfiguration.getGroceriesCode(),
                        new CategoryLookbackInformation(
                                "Kommer du ihåg att du den här dagen {0} ställde till med kalas?",
                                "Kommer du ihåg den här dagen {0} då handlade mat på så många ställen att du tappade räkningen?",
                                Lists.newArrayList("cover_groceries_1.jpg", "cover_groceries_2.jpg"),
                                "Den {0} hade du ditt största livsmedelsköp på hela året {1}",
                                "{0} livsmedelsköp på en dag", "Den här var dagen du hade flest livsmedelsköp {0}"));

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getClothesCode(),
                new CategoryLookbackInformation("Kommer du ihåg den här dagen {0} när du shoppade en ny garderob?",
                        "Minns du den här dagen {0} när du efter mycket letande hittade den perfekta outfitten?", Lists
                        .newArrayList("cover_clothes_women.jpg", "cover_clothes_men.jpg"),
                        "Den {0} hade du din största klädutgift på hela året {1}", "{0} klädköp på en dag",
                        "Den här var dagen du hade flest klädköp {0}"));

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getElectronicsCode(),
                new CategoryLookbackInformation("Minns du alla prylar du köpte den här dagen {0}?",
                        "Minns du alla prylar du köpte den här dagen {0}?", Lists.newArrayList(
                        "cover_electronics_1.jpg", "cover_electronics_2.jpg", "cover_electronics_3.jpg"),
                        "Den {0} hade du din största hemelektronikutgift på hela året {1}",
                        "{0} elektronikköp på en dag", "Den här var dagen du hade flest elektronikköp {0}"));

        return categoryInformationByCategoryCode;
    }

    private ImmutableMap<String, Category> categoriesById;

    private Map<String, CategoryLookbackInformation> categoryInformationByCategoryCode;

    public LookbackActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(LookbackActivityGenerator.class, 60, deepLinkBuilderFactory);

        minIosVersion = "1.6.2";
        minAndroidVersion = "1.8.6";
    }

    private List<Activity> createActivities(String userId, String trackingName, String categoryCode, Catalog catalog,
            Date activityDate, String question, String image, double value, String title, String subtitle,
            String description) {

        String key = getKey(categoryCode, activityDate);

        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

        List<Activity> activityList = Lists.newArrayList();
        activityList.add(createNativeActivity(userId, trackingName, categoryCode, catalog, activityDate, question,
                image, value, title, subtitle, description, key, feedActivityIdentifier));

        activityList.add(createHTMLActivity(userId, trackingName, categoryCode, catalog, activityDate, question, image,
                value, title, subtitle, description, key, feedActivityIdentifier));

        return activityList;
    }

    private String getKey(String categoryCode, Date activityDate) {
        return categoryCode + activityDate.getTime();
    }

    private Activity createNativeActivity(String userId, String trackingName,
            String categoryCode, Catalog catalog, Date activityDate,
            String question, String image, double value, String title,
            String subtitle, String description, String key, String feedActivityIdentifier) {

        LookbackActivityContent content = new LookbackActivityContent();
        content.setQuestion(question);
        content.setLookbackTime(catalog.getString("1 year ago"));
        content.setButtonLabel(catalog.getString("Look back on it"));
        return createActivity(userId, activityDate, Activity.Types.LOOKBACK, null, null, content, key,
                feedActivityIdentifier);
    }

    private Activity createHTMLActivity(String userId, String trackingName,
            String categoryCode, Catalog catalog, Date activityDate,
            String question, String image, double value, String title,
            String subtitle, String description, String key, String feedActivityIdentifier) {

        ShareableDetailsHtmlActivityData data = new ShareableDetailsHtmlActivityData();

        String feedHead = TemplateUtils.getTemplate("data/templates/lookback/head-feed.html");
        String detailsHead = TemplateUtils.getTemplate("data/templates/lookback/head-details.html");

        String feedBody = Catalog.format(TemplateUtils.getTemplate("data/templates/lookback/feed.html"),
                catalog.getString("1 year ago"), question);

        char rawIcon = TinkIconUtils.getV1CategoryIcon(categoryCode);

        String icon = StringEscapeUtils.escapeHtml(Character.toString(rawIcon));

        String detailsBody = Catalog.format(TemplateUtils.getTemplate("data/templates/lookback/details.html"), image,
                catalog.getString("1 year ago"), icon, title, subtitle, description);

        data.setTrackingName(String.format("Lookback (%s)", trackingName));
        data.setIcon(new HtmlActivityIconData("lookback", "pink"));
        data.setActivityHtml(createHtml(feedHead, feedBody));
        data.setDetailsHtml(createHtml(detailsHead, detailsBody));
        data.setButtonLabel(catalog.getString("Look back"));
        data.setShareableMessage(Catalog.format(catalog.getString("1 year ago: {0} - via @tink"), title));

        return createActivity(userId, activityDate, "html/shareable-details", null, null, data, key,
                feedActivityIdentifier);
    }

    /**
     * Filters the activities so we don't get too many and add them to the activity context.
     *
     * @param context
     */
    private void filterAndAddActivities(List<Activity> activities, ActivityGeneratorContext context) {
        Map<String, Long> lastActivityTimeByType = Maps.newHashMap();

        List<Activity> sortedActivities = ACTIVITIES_ORDERING.sortedCopy(activities);

        for (Activity activity : sortedActivities) {
            if (lastActivityTimeByType.get(activity.getType()) == null) {
                lastActivityTimeByType.put(activity.getType(), 0L);
            }
            if (activity.getDate().getTime() - lastActivityTimeByType.get(activity.getType())
                    < MIN_ACTIVITY_PERIODICITY) {
                continue;
            }
            lastActivityTimeByType.put(activity.getType(), activity.getDate().getTime());
            context.addActivity(activity);
        }
    }

    @Override
    public void generateActivity(final ActivityGeneratorContext context) {
        // Only generate activities for users in that test.

        if (!context.getUser().getFlags().contains(FeatureFlags.TEST_BADGES_AND_LOOKBACK)
                || !context.getUser().getProfile().getLocale().equals("sv_SE")) {
            return;
        }

        // Construct the required data.

        categoryInformationByCategoryCode = constructCategoryInformation(context.getCatalog(),
                context.getCategoryConfiguration());

        categoriesById = Maps.uniqueIndex(Iterables.filter(context.getCategories(),
                c -> categoryInformationByCategoryCode.containsKey(c.getCode())), Category::getId);

        final Calendar today = DateUtils.getCalendar();
        DateUtils.setInclusiveEndTime(today);

        List<Activity> activities = Lists.newArrayList();

        activities.addAll(generateActivityForYear(today, 2014, context));
        activities.addAll(generateActivityForYear(today, 2013, context));

        filterAndAddActivities(activities, context);
    }

    private List<Activity> generateActivityForYear(Calendar today, int year, final ActivityGeneratorContext context) {
        List<Activity> activities = Lists.newArrayList();

        // Filter out the statistics for last year.

        final Calendar yearEndDate = DateUtils.getCalendar();
        yearEndDate.set(Calendar.YEAR, year);
        yearEndDate.set(Calendar.MONTH, Calendar.DECEMBER);
        yearEndDate.set(Calendar.DAY_OF_MONTH, 31);
        DateUtils.setInclusiveEndTime(yearEndDate);

        final Calendar yearStartDate = DateUtils.getCalendar();
        yearEndDate.set(Calendar.YEAR, year);
        yearStartDate.set(Calendar.MONTH, Calendar.JANUARY);
        yearStartDate.set(Calendar.DAY_OF_MONTH, 1);
        DateUtils.setInclusiveStartTime(yearStartDate);

        final String yearName = ThreadSafeDateFormat.FORMATTER_YEARLY.format(yearEndDate.getTime());

        String lastYearDescription = Catalog.format(context.getCatalog().getString("in {0}"), yearName);

        ImmutableListMultimap<String, Statistic> lastYearStatisticsByCategoryCode = Multimaps.index(
                Iterables.filter(context.getStatistics(), s -> (s.getResolution() == ResolutionTypes.DAILY
                        && categoriesById.containsKey(s.getDescription()) && s.getPeriod()
                        .startsWith(yearName))), s -> categoriesById.get(s.getDescription()).getCode());

        ImmutableListMultimap<String, Transaction> lastYearTransactionByCategoryCode = Multimaps.index(
                Iterables.filter(context.getTransactions(), t -> (categoriesById.containsKey(t.getCategoryId())
                        && t.getDate().before(yearEndDate.getTime()) && t.getDate().after(
                        yearStartDate.getTime()))), t -> categoriesById.get(t.getCategoryId()).getCode());

        // Generate the activities.

        DateFormat dateFormatLong = DateFormat.getDateInstance(DateFormat.LONG,
                Catalog.getLocale(context.getUser().getProfile().getLocale()));
        DateFormat dateFormatFull = DateFormat.getDateInstance(DateFormat.FULL,
                Catalog.getLocale(context.getUser().getProfile().getLocale()));

        try {
            for (String categoryCode : lastYearStatisticsByCategoryCode.keySet()) {
                CategoryLookbackInformation categoryInformation = categoryInformationByCategoryCode.get(categoryCode);

                Iterable<Transaction> transactionsForCategory = lastYearTransactionByCategoryCode.get(categoryCode);

                Iterable<Statistic> transactionCountsForCategory = Iterables.filter(
                        lastYearStatisticsByCategoryCode.get(categoryCode),
                        s -> (Statistic.Types.EXPENSES_COUNT_BY_CATEGORY.equals(s.getType())));

                // Generate an activity for the day with the largest transaction.

                if (!Iterables.isEmpty(transactionsForCategory)) {
                    Transaction largestTransaction = TRANSACTION_ORDERING.max(transactionsForCategory);

                    double amount = Math.abs(largestTransaction.getAmount());

                    if (amount >= 100) {
                        Calendar calendar = DateUtils.getCalendar();
                        calendar.setTime(largestTransaction.getDate());
                        calendar.add(Calendar.YEAR, 1);

                        if (today.getTime().after(calendar.getTime())) {
                            activities.addAll(createActivities(
                                    context.getUser().getId(),
                                    "lookback/transaction/" + categoryCode,
                                    categoryCode,
                                    context.getCatalog(),
                                    DateUtils.flattenTime(calendar.getTime()),
                                    Catalog.format(categoryInformation.getQuestionAmount(), lastYearDescription),
                                    categoryInformation.getImage(),
                                    amount,
                                    largestTransaction.getDescription(),
                                    I18NUtils.formatCurrency(amount, context.getUserCurrency(), context.getLocale()),
                                    Catalog.format(categoryInformation.getStatementAmount(),
                                            dateFormatLong.format(largestTransaction.getDate()), lastYearDescription)));
                        }
                    }
                }

                // Generate an activity for the day with the most number of transaction.

                if (!Iterables.isEmpty(transactionCountsForCategory)) {
                    Statistic largestTransactionCount = STATISTICS_ORDERING.max(transactionCountsForCategory);

                    int count = (int) largestTransactionCount.getValue();

                    if (count >= 3) {
                        Date transactionsDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(largestTransactionCount
                                .getPeriod());

                        Calendar activityCalendar = DateUtils.getCalendar();
                        activityCalendar.setTime(transactionsDate);
                        activityCalendar.add(Calendar.YEAR, 1);

                        if (today.getTime().after(activityCalendar.getTime())) {
                            activities.addAll(createActivities(context.getUser().getId(),
                                    "lookback/category/" + categoryCode, categoryCode,
                                    context.getCatalog(), activityCalendar.getTime(),
                                    Catalog.format(categoryInformation.getQuestionCount(), lastYearDescription),
                                    categoryInformation.getImage(), count,
                                    Catalog.format(categoryInformation.getTitleCount(), count),
                                    dateFormatFull.format(transactionsDate),
                                    Catalog.format(categoryInformation.getStatementCount(), lastYearDescription)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(context.getUser().getId(), "Could not generate lookback activities", e);
        }

        return activities;
    }

    /**
     * Since we are constructing both native (new) and html (old), only generate notification for native.
     */
    @Override
    public List<Notification> generateNotifications(Activity activity, ActivityGeneratorContext context) {
        if (!Objects.equals(activity.getType(), Activity.Types.LOOKBACK)) {
            return Lists.newArrayList();
        }

        return super.generateNotifications(activity, context);
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
