package se.tink.backend.common.workers.activity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.core.Activity;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.Notification;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public abstract class ActivityGenerator {
    private static final double DEFAULT_IMPORTANCE = 10;
    private static final int MAX_MULTIPLE_TITLES = 5;
    protected static final double DEFAULT_AMOUNT_NORMALIZATION_FACTOR = 500;

    /**
     * Helper function to format a human readable list of titles.
     */
    protected static String formatMultipleTitles(Iterable<String> descriptions, Catalog catalog) {
        Iterator<String> descriptionIterator = descriptions.iterator();
        Set<String> included = Sets.newHashSetWithExpectedSize(MAX_MULTIPLE_TITLES);
        while (descriptionIterator.hasNext() && included.size() < MAX_MULTIPLE_TITLES) {
            included.add(descriptionIterator.next());
        }

        return I18NUtils.joinHuman(catalog, ImmutableList.copyOf(included));
    }

    protected static Iterable<Transaction> filterPeriodTransactions(Iterable<Transaction> transactions, String period,
                                                                    ResolutionTypes periodMode, int periodAdjustedDay) {
        final Date periodStartDate = DateUtils.getCurrentOrPreviousBusinessDay(DateUtils.getFirstDateFromPeriod(period,
                periodMode, periodAdjustedDay));

        final Date periodEndDate = DateUtils.getCurrentOrPreviousBusinessDay(DateUtils.getLastDateFromPeriod(period,
                periodMode, periodAdjustedDay));

        return Iterables.filter(transactions,
                t -> (t.getDate().getTime() >= periodStartDate.getTime() && t.getDate().getTime() <= periodEndDate
                        .getTime()));
    }

    private final Class<? extends ActivityGenerator> generatorClass;
    private final double maximumImportance;
    private DeepLinkBuilderFactory deepLinkBuilderFactory;
    private final double minimumImportance;

    private final LogUtils log;
    // Set these if a activity requires a certain version of the app.
    protected String minIosVersion;
    protected String minAndroidVersion;
    protected String maxIosVersion;
    protected String maxAndroidVersion;

    public ActivityGenerator(Class<? extends ActivityGenerator> generatorClass, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        this(generatorClass, DEFAULT_IMPORTANCE, deepLinkBuilderFactory);
    }

    public ActivityGenerator(Class<? extends ActivityGenerator> generatorClass, double defaultImportance,
            DeepLinkBuilderFactory deepLinkBuilderFactory) {
        this(generatorClass, defaultImportance, Math.min(100, defaultImportance * 2), deepLinkBuilderFactory);
    }

    public ActivityGenerator(Class<? extends ActivityGenerator> generatorClass, double minimumImportance,
            double maximumImportance, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        this.log = new LogUtils(generatorClass);
        this.generatorClass = generatorClass;

        this.minimumImportance = minimumImportance;
        this.maximumImportance = maximumImportance;
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    protected double calculateImportance(double value, double normalize) {
        return Math.min(minimumImportance + ((value / normalize) * (maximumImportance - minimumImportance)),
                maximumImportance);
    }

    protected Activity createActivity(String userId, Date date, String type, String title, String message,
            Object content) {
        return createActivity(userId, date, type, title, message, content, null, null);
    }

    protected Activity createActivity(String userId, Date date, String type, String title, String message, Object content, String key,
            String feedActivityIdentifier) {
        return createActivity(userId, date, type, title, message, content, key, feedActivityIdentifier, minimumImportance);
    }

    protected Activity createActivity(String userId, Date date, String type, String title, String message, Object content, String key,
            String feedActivityIdentifier, double importance) {
        return createActivity(userId, date, type, title, message, null, content, key, feedActivityIdentifier, importance);
    }

    protected Activity createActivity(String userId, Date date, String type, String title, String message, String sensitiveMessage,
            Object content, String key, String feedActivityIdentifier, double importance) {

        Activity activity = new Activity();

        activity.setUserId(userId);
        activity.setDate(date);
        activity.setType(type);
        activity.setMessage(message);
        activity.setSensitiveMessage(sensitiveMessage);
        activity.setGenerator(generatorClass.getSimpleName());
        activity.setImportance(importance);
        activity.setContent(content);
        activity.setKey(key);
        activity.setFeedActivityIdentifier(feedActivityIdentifier);
        activity.setTitle(title);
        activity.setMinIosVersion(minIosVersion);
        activity.setMaxIosVersion(maxIosVersion);
        activity.setMinAndroidVersion(minAndroidVersion);
        activity.setMaxAndroidVersion(maxAndroidVersion);

        return activity;
    }

    protected List<Notification> createNotifications(Activity activity, ActivityGeneratorContext context) {
        if (activity.getKey() == null) {
            return Collections.emptyList();
        }

        Notification.Builder builder = new Notification.Builder()
                .fromActivity(activity)
                .groupable(true)
                .url(deepLinkBuilderFactory.open().build()); // Feed.

        return buildNotificationsSilentlyFailing(activity.getUserId(), builder);
    }

    protected List<Notification> buildNotificationsSilentlyFailing(String userId, Notification.Builder builder) {
        try {
            return Collections.singletonList(builder.build());
        } catch (IllegalArgumentException e) {
            log.error(userId, "Could not generate notification", e);
            return Lists.newArrayList();
        }
    }

    public abstract void generateActivity(ActivityGeneratorContext context);

    public abstract boolean isNotifiable();

    public List<Notification> generateNotifications(Activity activity, ActivityGeneratorContext context) {

        if (!isNotifiable()) {
            return Collections.emptyList();
        }

        List<Notification> generatedNotifications = createNotifications(activity, context);

        // Filter the notifications based on what was previously generated.

        List<Notification> filteredNotifications = Lists.newArrayList();

        for (Notification notification : generatedNotifications) {
            if (context.getNotificationsByKey().containsKey(notification.getKey())) {
                continue;
            }

            filteredNotifications.add(notification);
        }

        // Add the notifications to the context and return the filtered list.

        context.addNotifications(filteredNotifications);

        return filteredNotifications;
    }

    /**
     * Group multiple similar activities to a single activity.
     */
    public List<Activity> groupActivities(ActivityGeneratorContext context, List<Activity> activities) {
        return activities;
    }

    protected List<KVPair<String, Double>> zeroFillActivityDataPoints(List<KVPair<String, Double>> dataPoints,
            ResolutionTypes resolution) {
        try {
            if (dataPoints.size() == 0) {
                return dataPoints;
            }

            Map<String, KVPair<String, Double>> dataPointsByKey = Maps.uniqueIndex(dataPoints,
                    KVPair::getKey);

            Ordering<Comparable> stringOrdering = Ordering.natural();
            String firstKey = stringOrdering.min(dataPointsByKey.keySet());
            String lastKey = stringOrdering.max(dataPointsByKey.keySet());

            ThreadSafeDateFormat dateFormat;
            int fieldToIncrement;

            switch (resolution) {
            case YEARLY:
                dateFormat = ThreadSafeDateFormat.FORMATTER_YEARLY;
                fieldToIncrement = Calendar.YEAR;
                break;
            case MONTHLY:
            case MONTHLY_ADJUSTED:
                dateFormat = ThreadSafeDateFormat.FORMATTER_MONTHLY;
                fieldToIncrement = Calendar.MONTH;
                break;
            case DAILY:
                dateFormat = ThreadSafeDateFormat.FORMATTER_DAILY;
                fieldToIncrement = Calendar.DAY_OF_YEAR;
                break;
            default:
                throw new RuntimeException("unsupported resolution: " + resolution);
            }

            Calendar date = DateUtils.getCalendar();
            date.setTime(DateUtils.flattenTime(dateFormat.parse(firstKey)));

            Date lastDate = DateUtils.flattenTime(dateFormat.parse(lastKey));

            List<KVPair<String, Double>> output = Lists.newArrayList();

            while (!date.getTime().after(lastDate)) {
                String key = dateFormat.format(date.getTime());

                KVPair<String, Double> dataPoint = dataPointsByKey.get(key);

                if (dataPoint == null) {
                    dataPoint = new KVPair<>(key, 0d);
                }

                output.add(dataPoint);

                date.add(fieldToIncrement, 1);
            }

            return output;
        } catch (ParseException e) {
            log.error("Could not zero-fill data points", e);
            return dataPoints;
        }
    }
}
