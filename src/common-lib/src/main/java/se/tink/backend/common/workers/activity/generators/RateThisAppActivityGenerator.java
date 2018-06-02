package se.tink.backend.common.workers.activity.generators;

import java.util.Date;
import se.tink.backend.common.config.RateThisAppConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.renderers.models.RateThisAppContent;
import se.tink.backend.core.Activity;
import se.tink.backend.core.UserState;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;

public class RateThisAppActivityGenerator extends ActivityGenerator {

    private static final String STATUS_IGNORE = "user-clicked-ignore";
    private static final String STATUS_RATE_IN_STORE = "user-clicked-rate-in-store";
    private static final String ACTIVITY_KEY = "rate-this-app";
    private DeepLinkBuilderFactory deepLinkBuilderFactory;

    public RateThisAppActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory,
            ServiceConfiguration configuration) {
        super(RateThisAppActivityGenerator.class, 70, 90, deepLinkBuilderFactory);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;

        if (configuration.getCluster() == Cluster.TINK) {
            minIosVersion = "2.5.19";
            minAndroidVersion = "3.0.26";
        } else {
            minIosVersion = "2.0.1";
            minAndroidVersion = "1.4.0";
        }
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        if (!isEnabled(context) || !isActiveUser(context) || hasInteractedWithActivity(context.getUserState())) {
            // Only show this activity to "active users" and users that have not interacted with the activity already.
            return;
        }

        context.addActivity(
                createActivity(
                        context.getUser().getId(),
                        DateUtils.getToday(),
                        Activity.Types.RATE_THIS_APP,
                        null,
                        null,
                        getContent(),
                        ACTIVITY_KEY,
                        StringUtils.hashAsStringSHA1(ACTIVITY_KEY)));
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }

    private RateThisAppContent getContent() {
        RateThisAppContent content = new RateThisAppContent();
        content.setDeepLinkIgnore(deepLinkBuilderFactory.rateThisApp().withStatus(STATUS_IGNORE).build());
        content.setDeepLinkRateInStore(deepLinkBuilderFactory.rateThisApp().withStatus(STATUS_RATE_IN_STORE).build());
        return content;
    }

    private boolean hasInteractedWithActivity(UserState userState) {
        switch (userState.getRateThisAppStatus()) {
        case USER_CLICKED_IGNORE:
        case USER_CLICKED_RATE_IN_STORE:
            return true;
        default:
            return false;
        }
    }

    private boolean isActiveUser(ActivityGeneratorContext context) {
        RateThisAppConfiguration configuration = context.getServiceContext().getConfiguration().getActivities()
                .getRateThisApp();

        return context.getUserState().getAmountCategorizationLevel() >= configuration.getMinCategorizationLevel() &&
                context.getUserState().getInitialAmountCategorizationLevel() <= configuration
                        .getMaxInitialCategorization() &&
                context.getUser().getCreated()
                        .before(DateUtils.addDays(new Date(), -configuration.getMinDaysSinceCreated())) &&
                context.getUser().getCreated()
                        .after(DateUtils.addDays(new Date(), -configuration.getMaxDaysSinceCreated()));
    }

    private boolean isEnabled(ActivityGeneratorContext context) {
        return context.getServiceContext().getConfiguration().getActivities().getRateThisApp().isEnabled();
    }
}
