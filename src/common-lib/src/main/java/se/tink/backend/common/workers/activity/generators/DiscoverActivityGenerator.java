package se.tink.backend.common.workers.activity.generators;

import java.util.Date;
import java.util.List;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.DiscoverActivityData;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.follow.FollowItem;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class DiscoverActivityGenerator extends ActivityGenerator {

    // Number of days after registration for the user to discover features themselves.
    private static final int SLACK = 5; 
    
    public DiscoverActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(DiscoverActivityGenerator.class, 10, deepLinkBuilderFactory);
        
        minIosVersion = "1.3.0";
    }
    
    @Override
    public void generateActivity(ActivityGeneratorContext context) {

        if (!context.getUser().getFlags().contains(FeatureFlags.TEST_DISCOVER_ON)) {
            return;
        }
        
        int daysSinceCreated = DateUtils.daysBetween(context.getUser().getCreated(), new Date());

        if (daysSinceCreated < SLACK) {
            return;
        }

        if (shouldGenerateDiscoverBudgetsActivity(context)) {
            context.addActivity(generateDiscoverBudgetsActivity(context));
        } else {
            context.addActivity(generateEmptyActivity(context.getUser().getId()));
        }

        // Disable the discover categories activity until we have decided what we should do with it /Erik
        //context.addActivity(generateDiscoverCategoriesActivity(context));
    }

    private boolean shouldGenerateDiscoverBudgetsActivity(ActivityGeneratorContext context) {

        // Generate the "discover budgets" activity only if the user doesn't already have any budgets.
        
        List<FollowItem> followItems = context.getFollowItems();
        return followItems == null || followItems.isEmpty();
    }
    
    private Activity generateDiscoverBudgetsActivity(ActivityGeneratorContext context) {
        Catalog catalog = context.getCatalog();

        // Set the date of the activity to a (potentially) future date to make it sticky on top of the feed a few days
        // before slowly decaying. For users that just signed up, this date is based on the creation date.
        // For old users, it's instead based on the start day of the Discover Budgets activity.
        int daysOfBeingSticky = 7;
        Date startDateBasedOnUserCreated = DateUtils.addDays(context.getUser().getCreated(), SLACK);
        Date startOfDiscoverBudgets = DateUtils.parseDate("2016-01-26");
        Date baseDate = DateUtils.max(startDateBasedOnUserCreated, startOfDiscoverBudgets);
        Date date = DateUtils.addDays(baseDate, daysOfBeingSticky);

        String key = getKey(Activity.Types.DISCOVER_BUDGETS, date); 
        String identifier = getIdentifier(key);

        DiscoverActivityData data = new DiscoverActivityData();
        data.setButtonText(catalog.getString("Add a budget now"));
        data.setImage("data/images/svg/abnamro-discover-budgets.svg");
        
        return createActivity(
                context.getUser().getId(),
                date,
                Activity.Types.DISCOVER_BUDGETS,
                catalog.getString("Tip: start with budgetting"),
                catalog.getString("For example, set a maximum amount per month on groceries or clothes and Grip will track it for you."),
                data,
                key,
                identifier);
    }
    
    private Activity generateDiscoverCategoriesActivity(ActivityGeneratorContext context) {

        // Set the date of the activity to a (potentially) future date to make it sticky on top of the feed a few days
        // before slowly decaying. For users that just signed up, this date is based on the creation date.
        // For old users, it's instead based on the start day of the Discover Budgets activity.
        int daysOfBeingSticky = 14;
        Date startDateBasedOnUserCreated = DateUtils.addDays(context.getUser().getCreated(), SLACK);
        Date startOfDiscoverCategories = DateUtils.parseDate("2016-04-14");
        Date baseDate = DateUtils.max(startDateBasedOnUserCreated, startOfDiscoverCategories);
        Date date = DateUtils.addDays(baseDate, daysOfBeingSticky);

        String key = getKey(Activity.Types.DISCOVER_CATEGORIES, date); 
        String identifier = getIdentifier(key);

        DiscoverActivityData data = new DiscoverActivityData();
        data.setButtonText("Lees meer");
        
        return createActivity(
                context.getUser().getId(),
                date,
                Activity.Types.DISCOVER_CATEGORIES,
                "Categorieën & tagging vernieuwd",
                "Op basis van feedback van gebruikers hebben wij een aantal categorieën aangepast. Bovendien hebben we 'Tagging' verbeterd. Benieuwd naar de veranderingen?",
                data,
                key,
                identifier);
    }
    
    private Activity generateEmptyActivity(String userId) {

        Date date = new Date();
        String key = getKey(Activity.Types.DISCOVER_EMPTY, date);
        String identifier = getIdentifier(key);

        return createActivity(userId, date, Activity.Types.DISCOVER_EMPTY, null, null, null, key, identifier);
    }
    
    private String getKey(String type, Date date) {
        return String.format("%s.%s", type, ThreadSafeDateFormat.FORMATTER_DAILY.format(date));
    }
    
    private String getIdentifier(String key) {
        return StringUtils.hashAsStringSHA1(key);
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
