package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.Activity;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsActivityData;
import se.tink.backend.core.FraudItem;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.Notification;
import se.tink.backend.utils.StringUtils;

public class FraudDetailsActivityGenerator extends CustomHtmlActivtyGenerator {

    private final Ordering<Notification> dateOrdering = new Ordering<Notification>() {
        @Override
        public int compare(Notification left, Notification right) {
            return ComparisonChain.start().compare(left.getDate(), right.getDate())
                    .compare(left.getId(), right.getId()).result();
        }
    };
    private DeepLinkBuilderFactory deepLinkBuilderFactory;

    public FraudDetailsActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(FraudDetailsActivityGenerator.class, 60, 70, deepLinkBuilderFactory);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
        minIosVersion = "2.0.0";
        minAndroidVersion = "1.8.5";
    }

    @Override
    protected List<Notification> createNotifications(Activity activity, ActivityGeneratorContext context) {
        // Check if new activity has date older than latest old notification.

        Iterable<Notification> oldNotifications = Iterables.filter(context.getNotifications(),
                n -> Objects.equals(n.getType(), "fraud-warning"));

        if (Iterables.size(oldNotifications) != 0) {

            // Order notifications on date (which is the created date for the fraud details).

            final Date latestFraudReminderNotification = dateOrdering.max(oldNotifications).getDate();

            // Check if this activity is created after the last notification.

            if (activity.getDate().before(latestFraudReminderNotification)) {
                return Lists.newArrayList();
            }
        }

        Notification.Builder notification = new Notification.Builder()
                .fromActivity(activity)
                .type("fraud-warning")
                .groupable(false)
                .url(deepLinkBuilderFactory.fraudWarning().build());

        return buildNotificationsSilentlyFailing(activity.getUserId(), notification);
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        // Doing nothing here since this activity is based on the result of other activities.
        // Using this structure for super class methods.
    }

    public List<Activity> convertFromFraudDetails(FraudDataProcessorContext context) {

        int unhandledDetailsCount = 0;

        for (FraudItem item : context.getInStoreFraudItems()) {
            unhandledDetailsCount += item.getUnhandledDetailsCount();
        }

        // Nothing to handle, no activity needed.
        if (unhandledDetailsCount == 0) {
            return null;
        }

        List<FraudDetails> allFraudDetails = Lists.newArrayList();
        allFraudDetails.addAll(context.getInStoreFraudDetails());
        allFraudDetails.addAll(context.getInBatchFraudDetails());

        // Get latest date from fraud unhandled details.

        Iterable<FraudDetails> unhandledDetails = Iterables.filter(allFraudDetails,
                fd -> fd.getStatus() != FraudStatus.OK && fd.getStatus() != FraudStatus.EMPTY);

        List<Activity> activities = Lists.newLinkedList();

        activities.add(createNativeActivity(unhandledDetails, unhandledDetailsCount, context));

        return activities;
    }

    private Activity createNativeActivity(Iterable<FraudDetails> unhandledDetails, int unhandledDetailsCount,
            FraudDataProcessorContext context) {

        FraudDetailsActivityData data = new FraudDetailsActivityData();

        Date date = FraudUtils.CREATED_ORDERING.max(unhandledDetails).getCreated();
        String locale = context.getUser().getProfile().getLocale();
        Catalog catalog = Catalog.getCatalog(locale);
        String key = FraudUtils.generateNotificationKey(Activity.Types.FRAUD + "-warning", date);
        String title = catalog.getString("ID Control");

        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

        data.setUnhandledDetailsCount(unhandledDetailsCount);

        return createActivity(context.getUser().getId(), date, Activity.Types.FRAUD, title,
                getMessage(catalog, unhandledDetailsCount), data, key, feedActivityIdentifier);
    }

    private String getMessage(Catalog catalog, int unhandledDetailsCount) {
        return String.format(catalog.getPluralString(
                "There is %s new warning that needs your attention.",
                "There are %s new warnings that need your attention.", unhandledDetailsCount),
                unhandledDetailsCount);
    }

    /**
     * Since we are constructing both native (new) and html (old), only generate notification for native.
     */
    @Override
    public List<Notification> generateNotifications(Activity activity, ActivityGeneratorContext context) {
        if (!Objects.equals(activity.getType(), Activity.Types.FRAUD)) {
            return Lists.newArrayList();
        }

        return super.generateNotifications(activity, context);
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
