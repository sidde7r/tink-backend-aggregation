package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
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
import se.tink.backend.core.BadgeActivityContent;
import se.tink.backend.core.Category;
import se.tink.backend.core.Notification;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class BadgeActivityGenerator extends CustomHtmlActivtyGenerator {
    private static final long MIN_ACTIVITY_PERIODICITY = 2 * 7 * 24 * 60 * 60 * 1000;

    private static class CategoryBadgeInformation {
        private static final Random RANDOM = new Random();

        private static String getRandomString(List<String> descriptions) {
            synchronized (RANDOM) {
                return descriptions.get(RANDOM.nextInt(descriptions.size()));
            }
        }

        private final int amountFactor;
        private final List<String> categoryAmountDescriptions;
        private final List<String> categoryCountDescriptions;
        private final List<String> categoryImages;
        private final List<String> categoryTitles;
        private final int frequencyFactor;
        private final List<String> merchantAmountDescriptions;
        private final List<String> merchantCountDescriptions;
        private final double transactionFactor;

        public CategoryBadgeInformation(int frequencyFactor, int amountFactor, double transactionFactor,
                List<String> categoryImages, List<String> categoryTitles, List<String> categoryCountDescriptions,
                List<String> categoryAmountDescriptions, List<String> merchantCountDescriptions,
                List<String> merchantAmountDescriptions) {
            this.frequencyFactor = frequencyFactor;
            this.amountFactor = amountFactor;
            this.transactionFactor = transactionFactor;
            this.categoryImages = categoryImages;
            this.categoryTitles = categoryTitles;
            this.categoryCountDescriptions = categoryCountDescriptions;
            this.categoryAmountDescriptions = categoryAmountDescriptions;
            this.merchantCountDescriptions = merchantCountDescriptions;
            this.merchantAmountDescriptions = merchantAmountDescriptions;
        }

        public int getAmountFactor() {
            return amountFactor;
        }

        public String getCategoryAmountDescription() {
            return getRandomString(categoryAmountDescriptions);
        }

        public String getCategoryCountDescription() {
            return getRandomString(categoryCountDescriptions);
        }

        public String getCategoryImage() {
            return getRandomString(categoryImages);
        }

        public String getCategoryTitle() {
            return getRandomString(categoryTitles);
        }

        public int getFrequencyFactor() {
            return frequencyFactor;
        }

        public String getMerchantAmountDescription() {
            return getRandomString(merchantAmountDescriptions);
        }

        public String getMerchantCountDescription() {
            return getRandomString(merchantCountDescriptions);
        }

        public double getTransactionFactor() {
            return transactionFactor;
        }
    }

    private static final String BADGE_AMOUNT = "badge.svg";
    private static final String BADGE_COUNT = "badge-count.svg";
    private static final int MONTHLY_TO_YEARLY_ADJUSTMENT_FACTOR = 10;

    private static final Ordering<Transaction> TRANSACTION_ORDERING = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction left, Transaction right) {
            return ComparisonChain.start().compare(left.getDate(), right.getDate())
                    .compare(left.getDescription(), right.getDescription()).compare(left.getId(), right.getId())
                    .result();
        }
    };

    private static Map<String, CategoryBadgeInformation> constructCategoryInformation(Catalog catalog,
            CategoryConfiguration categoryConfiguration) {
        Map<String, CategoryBadgeInformation> categoryInformationByCategoryCode = Maps.newHashMap();

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getGroceriesCode(),
                new CategoryBadgeInformation(5, 500, 0.5, Lists.newArrayList("cover_groceries_1.jpg",
                        "cover_groceries_2.jpg"), Lists.newArrayList("Snart Jamie Oliver!", "Mästerkockarnas mästare",
                        "Food lover"), Lists.newArrayList("Du har handlat mat {0} gånger {1}!",
                        "Du har varit i matbutiken {0} gånger {1}!"), Lists
                        .newArrayList("Du har spenderat {0} på livsmedel {2}!"), Lists
                        .newArrayList("Du har handlat mat på {0} {1} gånger {2}!"), Lists
                        .newArrayList("Du har spenderat {0} på mat på {1} {2}!")));

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getRestaurantsCode(),
                new CategoryBadgeInformation(5, 500, 0.5, Lists.newArrayList("cover_food.jpg",
                        "cover_restaurants_1.jpg", "cover_restaurants_2.jpg", "cover_restaurants_3.jpg"), Lists
                        .newArrayList("Krogbesökaren", "Matälskaren", "Gourmanden"), Lists
                        .newArrayList("Du har ätit ute {0} gånger {1}!"), Lists
                        .newArrayList("Du har spenderat {0} på att äta ute {2}!"), Lists
                        .newArrayList("Du har handlat mat på {0} {1} gånger {2}!"), Lists
                        .newArrayList("Du har spenderat {0} på mat på {1} {2}!")));

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getCoffeeCode(),
                new CategoryBadgeInformation(5, 500, 0.5, Lists.newArrayList("cover_coffee_1.jpg",
                        "cover_coffee_2.jpg", "cover_coffee_3.jpg"), Lists.newArrayList("Fika lover", "Sweetie Pie!",
                        "Kakmonster", "Kaffeälskaren"), Lists.newArrayList("Du har fikat {0} gånger {1}!"), Lists
                        .newArrayList("Du har spenderat {0} på fika {2}!"), Lists
                        .newArrayList("Du har fikat på {0} {1} gånger {2}!"), Lists
                        .newArrayList("Du har fikat för {0} på {1} {2}!")));

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getBarsCode(),
                new CategoryBadgeInformation(5, 500, 0.5, Lists.newArrayList("cover_bar_1.jpg", "cover_bar_2.jpg",
                        "cover_bar_3.jpg"), Lists.newArrayList("Weekend Dancer", "Party Animal", "Klubbkid",
                        "Nightcrawler"), Lists.newArrayList("Du har köpt drinkar ute {0} gånger {1}!"), Lists
                        .newArrayList("Du har köpt drinkar på {0} {1} gånger {2}!"), Lists
                        .newArrayList("Du har köpt drinkar på {0} {1} gånger {2}!"), Lists
                        .newArrayList("Du har spenderat {0} på drinkar på {1} {2}!")));

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getClothesCode(),
                new CategoryBadgeInformation(5, 500, 0.5, Lists.newArrayList("cover_clothes_women.jpg",
                        "cover_clothes_men.jpg"), Lists.newArrayList("Modeprofilen", "Fashion lover"), Lists
                        .newArrayList("Du har shoppat kläder & accessoarer {0} gånger {1}!"), Lists
                        .newArrayList("Du har spenderat {0} på kläder & accessoarer {2}!"), Lists
                        .newArrayList("Du har handlat kläder & accessoarer på {0} {1} gånger {2}!"), Lists
                        .newArrayList("Du har spenderat {0} på kläder & accessoarer på {1} {2}!")));

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getTaxiCode(),
                new CategoryBadgeInformation(5, 500, 0.5, Lists.newArrayList("cover_taxi_1.jpg", "cover_taxi_2.jpg"),
                        Lists.newArrayList("Taxi Rider", "Taxi Lover"), Lists
                        .newArrayList("Du har åkt taxi {0} gånger {1}!"), Lists
                        .newArrayList("Du har spenderat {0} på taxi {2}!"), Lists
                        .newArrayList("Du har åkt med {0} {1} gånger {2}!"), Lists
                        .newArrayList("Du har spenderat {0} på taxiresor på {1} {2}!")));

        categoryInformationByCategoryCode.put(
                categoryConfiguration.getElectronicsCode(),
                new CategoryBadgeInformation(5, 500, 0.5, Lists.newArrayList("cover_electronics_1.jpg",
                        "cover_electronics_2.jpg", "cover_electronics_3.jpg"), Lists.newArrayList("Prylsamlaren",
                        "Elektronikshopper"), Lists.newArrayList("Du har handlat elprylar {0} gånger {1}!"), Lists
                        .newArrayList("Du har spenderat {0} på elprylar {2}!"), Lists
                        .newArrayList("Du har handlat elprylar på {0} {1} gånger {2}!"), Lists
                        .newArrayList("Du har spenderat {0} på elprylar på {1} {2}!")));

        return categoryInformationByCategoryCode;
    }

    private ImmutableMap<String, Category> categoriesById;
    private Map<String, CategoryBadgeInformation> categoryInformationByCategoryCode;

    public BadgeActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(BadgeActivityGenerator.class, 60, deepLinkBuilderFactory);

        minIosVersion = "1.6.2";
        // minAndroidVersion = "1.0.0";
    }

    public static final Ordering<Activity> ACTIVITIES_ORDERING = new Ordering<Activity>() {
        @Override
        public int compare(Activity left, Activity right) {
            return ComparisonChain.start().compare(left.getDate(), right.getDate()).result();
        }
    };

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

    /**
     * Helper function to actually create the activity.
     *
     * @param context
     * @param trackingName
     * @param categoryIcon
     * @param badgeImage
     * @param value
     * @param image
     * @param title
     * @param date
     * @param description
     * @return
     */
    private List<Activity> createActivities(ActivityGeneratorContext context, String trackingName, char categoryIcon,
            String badgeImage, double value, String image, String title, Date date, String description, String key,
            String feedActivityIdentifier) {

        List<Activity> activityList = Lists.newArrayList();
        activityList.add(createNativeActivity(context, trackingName
                , categoryIcon, value, image, title, date, description, key, feedActivityIdentifier));
        activityList.add(createHTMLActivity(context, trackingName, categoryIcon, badgeImage, value, image, title,
                date, description, key, feedActivityIdentifier));

        return activityList;

    }

    /**
     * Helper function to actually create the HTML activity.
     *
     * @param context
     * @param trackingName
     * @param categoryIcon
     * @param badgeImage
     * @param value
     * @param image
     * @param title
     * @param date
     * @param description
     * @return
     */
    private Activity createHTMLActivity(ActivityGeneratorContext context, String trackingName, char categoryIcon,
            String badgeImage, double value, String image, String title, Date date, String description, String key,
            String feedActivityIdentifier) {
        // log.debug(date + ": " + title + " - " + description);

        ShareableDetailsHtmlActivityData data = new ShareableDetailsHtmlActivityData();

        String feedHead = TemplateUtils.getTemplate("data/templates/badges/head-feed.html");
        String detailsHead = TemplateUtils.getTemplate("data/templates/badges/head-details.html");

        String feedBody = Catalog.format(TemplateUtils.getTemplate("data/templates/badges/feed.html"), badgeImage,
                image, StringEscapeUtils.escapeHtml(Character.toString(categoryIcon)),
                I18NUtils.formatShort(value, context.getLocale()).toUpperCase(), title, description);

        String detailsBody = Catalog.format(TemplateUtils.getTemplate("data/templates/badges/details.html"),
                badgeImage, image, categoryIcon, I18NUtils.formatShort(value, context.getLocale()).toUpperCase(),
                title, description);

        data.setTrackingName(String.format("Badge (%s)", trackingName));
        data.setIcon(new HtmlActivityIconData("trophy", "pink"));
        data.setActivityHtml(createHtml(feedHead, feedBody));
        data.setDetailsHtml(createHtml(detailsHead, detailsBody));

        return createActivity(context.getUser().getId(), date, "html/shareable-details", null, null, data, key,
                feedActivityIdentifier);
    }

    /**
     * Helper function to actually create a native activity used for HTML feed.
     *
     * @param context
     * @param trackingName
     * @param categoryIcon
     * @param value
     * @param image
     * @param title
     * @param date
     * @param description
     * @return
     */
    private Activity createNativeActivity(ActivityGeneratorContext context, String trackingName, char categoryIcon,
            double value, String image, String title, Date date, String description, String key,
            String feedActivityIdentifier) {

        BadgeActivityContent content = new BadgeActivityContent();
        content.setBadgeImage(BADGE_AMOUNT);
        content.setImage(image);
        content.setCategoryIcon(categoryIcon);
        content.setFormattedValue(I18NUtils.formatShort(value, context.getLocale()).toUpperCase());
        content.setTitle(title);
        content.setDescription(description);

        return createActivity(context.getUser().getId(), date, Activity.Types.BADGE, null, null, content, key,
                feedActivityIdentifier);
    }

    private List<Activity> generateActivities(ActivityGeneratorContext context, ResolutionTypes resolution,
            boolean isMerchantBased, ImmutableListMultimap<String, Transaction> transactionsByKey) {
        List<Activity> activities = Lists.newArrayList();

        for (String key : transactionsByKey.keySet()) {
            ImmutableList<Transaction> transactions = transactionsByKey.get(key);

            // TODO: Not all transactions for a merchant will have the same category, figure out the most common instead
            // of using the first.

            String categoryId = transactions.get(0).getCategoryId();

            Category category = categoriesById.get(categoryId);

            char categoryIcon = TinkIconUtils.getV1CategoryIcon(category.getCode());
            // String categoryIcon = StringEscapeUtils.escapeHtml(Character.toString(TinkIconUtils
            // .getV1CategoryIcon(category.getCode())));

            CategoryBadgeInformation categoryBadgeInformation = categoryInformationByCategoryCode.get(category
                    .getCode());

            if (categoryBadgeInformation == null) {
                continue;
            }

            List<Transaction> sortedTransactions = TRANSACTION_ORDERING.sortedCopy(transactions);

            int frequencyFactor = categoryBadgeInformation.getFrequencyFactor();
            int amountFactor = categoryBadgeInformation.getAmountFactor();

            // Adjust the factors depending on the resolution.

            String periodDescription = null;

            switch (resolution) {
            case MONTHLY:
                periodDescription = context.getCatalog().getString("this month");

                break;
            case YEARLY:
                periodDescription = context.getCatalog().getString("this year");

                frequencyFactor = frequencyFactor * MONTHLY_TO_YEARLY_ADJUSTMENT_FACTOR;
                amountFactor = amountFactor * MONTHLY_TO_YEARLY_ADJUSTMENT_FACTOR;
                break;
            default:
                throw new RuntimeException("Not supporting resolution: " + resolution);
            }

            // Adjust the factors if we're in merchant mode.

            if (isMerchantBased) {
                frequencyFactor = (int) (frequencyFactor * categoryBadgeInformation.getTransactionFactor());
                amountFactor = (int) (amountFactor * categoryBadgeInformation.getTransactionFactor());
            }

            // Slight optimization not to loop through stuff we won't find anything in.

            if (sortedTransactions.size() < frequencyFactor) {
                continue;
            }

            // Loop through the transactions and find anything relevant.

            double amount = 0;
            int count = 0;

            double amountThreshold = amountFactor;
            double countThreshold = frequencyFactor;

            transactionLoop:
            for (Transaction transaction : sortedTransactions) {
                amount += Math.abs(transaction.getAmount());
                count++;

                if (count == 0) {
                    continue;
                }

                // Check if we've passed a count-based threshold.

                if (count == countThreshold) {
                    if (isMerchantBased) {
                        String description = Catalog.format(categoryBadgeInformation.getMerchantCountDescription(),
                                transaction.getDescription(), count, periodDescription);

                        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

                        activities.addAll(createActivities(context, "count/merchant/" + category.getCode(),
                                categoryIcon, BADGE_COUNT, count,
                                categoryBadgeInformation.getCategoryImage(), transaction.getDescription(),
                                transaction.getDate(), description, key, feedActivityIdentifier));
                    } else {
                        String description = Catalog.format(categoryBadgeInformation.getCategoryCountDescription(),
                                count, periodDescription);

                        String activityKey = getKey(category.getCode(), "count", count);

                        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

                        activities.addAll(createActivities(context, "count/category/" + category.getCode(),
                                categoryIcon, BADGE_COUNT, count,
                                categoryBadgeInformation.getCategoryImage(),
                                categoryBadgeInformation.getCategoryTitle(), transaction.getDate(),
                                description, activityKey, feedActivityIdentifier));
                    }

                    countThreshold += countThreshold;
                    continue transactionLoop;
                }

                // Check if we've passed an amount-based threshold.

                if (amount >= amountThreshold) {
                    if (isMerchantBased) {
                        String description = Catalog.format(categoryBadgeInformation.getMerchantAmountDescription(),
                                I18NUtils.formatCurrency(amountThreshold, context.getUserCurrency(),
                                        context.getLocale()),
                                transaction.getDescription(), periodDescription);

                        String activityKey = getKey(description, "amount", amountThreshold);

                        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

                        activities.addAll(createActivities(context, "amount/merchant/" + category.getCode(),
                                categoryIcon, BADGE_AMOUNT, amountThreshold,
                                categoryBadgeInformation.getCategoryImage(), transaction.getDescription(),
                                transaction.getDate(),
                                description, activityKey, feedActivityIdentifier));
                    } else {
                        String description = Catalog.format(categoryBadgeInformation.getCategoryAmountDescription(),
                                I18NUtils.formatCurrency(amountThreshold, context.getUserCurrency(),
                                        context.getLocale()),
                                category.getDisplayName(), periodDescription);

                        String activityKey = getKey(category.getCode(), "amount", amountThreshold);

                        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

                        activities.addAll(createActivities(context, "amount/category/" + category.getCode(),
                                categoryIcon, BADGE_AMOUNT, amountThreshold,
                                categoryBadgeInformation.getCategoryImage(),
                                categoryBadgeInformation.getCategoryTitle(), transaction.getDate(),
                                description, activityKey, feedActivityIdentifier));
                    }

                    amountThreshold += amountThreshold;
                    continue transactionLoop;
                }
            }
        }

        return activities;
    }

    private String getKey(String prefix, String badgeType, double amountThreshold) {
        return prefix + badgeType + amountThreshold;
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        // Only generate badges for users in that test and speaks swedish.

        if (!context.getUser().getFlags().contains(FeatureFlags.TEST_BADGES_AND_LOOKBACK)
                || !context.getUser().getProfile().getLocale().equals("sv_SE")) {
            return;
        }

        List<Activity> activities = Lists.newArrayList();

        // Construct the required data.

        categoriesById = Maps.uniqueIndex(context.getCategories(), Category::getId);

        categoryInformationByCategoryCode = constructCategoryInformation(context.getCatalog(),
                context.getCategoryConfiguration());

        // Index transactions by category and merchant.

        ImmutableListMultimap<String, Transaction> transactionsByYear = Multimaps.index(context.getTransactions(),
                t -> ThreadSafeDateFormat.FORMATTER_YEARLY.format(t.getDate()));

        for (String year : transactionsByYear.keySet()) {
            ImmutableListMultimap<String, Transaction> transactionsByMerchant = Multimaps.index(
                    transactionsByYear.get(year), Transaction::getDescription);

            ImmutableListMultimap<String, Transaction> transactionsByCategory = Multimaps.index(
                    transactionsByYear.get(year), Transaction::getCategoryId);

            // generateActivities(context, ResolutionTypes.MONTHLY, false, transactionsByCategory);
            activities.addAll(generateActivities(context, ResolutionTypes.YEARLY, false, transactionsByCategory));

            // generateActivities(context, ResolutionTypes.MONTHLY, true, transactionsByMerchant);
            activities.addAll(generateActivities(context, ResolutionTypes.YEARLY, true, transactionsByMerchant));
        }

        filterAndAddActivities(activities, context);
    }

    /**
     * Since we are constructing both native (new) and html (old), only generate notification for native.
     */
    @Override
    public List<Notification> generateNotifications(Activity activity, ActivityGeneratorContext context) {
        if (!Objects.equals(activity.getType(), Activity.Types.BADGE)) {
            return Lists.newArrayList();
        }

        return super.generateNotifications(activity, context);
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
