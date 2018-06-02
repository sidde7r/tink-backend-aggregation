package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.utils.LoanEventActivityHelper;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Loan;
import se.tink.backend.core.LoanEvent;
import se.tink.backend.core.LoanEventActivityData;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.LoanUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.application.ApplicationType;

public class LoanEventActivityGenerator extends ActivityGenerator {
    private DeepLinkBuilderFactory deepLinkBuilderFactory;

    private LoanEventActivityHelper activityHelper;

    public LoanEventActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(LoanEventActivityGenerator.class, 80, deepLinkBuilderFactory);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context){

        if (!FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE.isFlagInGroup(context.getUser().getFlags())) {
            return;
        }

        // Enable only for Tink employees and iOS beta users until quality assured.
        if (!context.getUser().getFlags().contains(FeatureFlags.TINK_EMPLOYEE) && !context.getUser().getFlags()
                .contains(FeatureFlags.IOS_BETA)) {
            return;
        }

        activityHelper = new LoanEventActivityHelper(context);

        final String userLocale = context.getUser().getLocale();
        final List<Account> accounts = context.getAccounts();

        // Map accountIds to their corresponding list of loans + filtering
        Map<String, List<Loan>> loansByAccountId = activityHelper.getLoansByAccountIds(accounts);
        Map<String, List<Loan>> filteredLoansByAccountId = LoanUtils.filterMapByLoanType(loansByAccountId, Loan.Type.MORTGAGE);

        // Make Loans into LoanEvents
        Map<String, List<LoanEvent>> loanEventsByAccountId = activityHelper.createLoanEventsByIdsAndLocale(
                filteredLoansByAccountId, userLocale);

        List<Activity> activities = createActivitiesByDateAndCredentials(loanEventsByAccountId, context.getUser().getId());

        context.addActivities(activities);
    }


    /**
     * Maps the loanEvents by dates and then credentials.
     */
    private List<Activity> createActivitiesByDateAndCredentials(Map<String, List<LoanEvent>> loansByAccount, String userId) {
        List<Activity> activities = Lists.newArrayList();

        // Map all LoanEvents to a LocalDate. Now we only need to separate them by credentials
        Map<LocalDate, List<LoanEvent>> loanEventsByDate = activityHelper.getLoanEventsByDate(loansByAccount);

        Map<LocalDate, Map<String, List<LoanEvent>>> loanEventsByDateByCredentials =
                Maps.transformValues(loanEventsByDate, activityHelper::splitLoanEventsByCredentials);

        loanEventsByDateByCredentials.values().forEach( loanEventsByCredentials ->
                activities.addAll(createActivitiesByCredentials(loanEventsByCredentials, userId))
        );

        return activities;
    }

    /**
     * Iterates through each credentials and creates an activity for each.
     */
    private List<Activity> createActivitiesByCredentials(Map<String, List<LoanEvent>> loanEventsByCredentials, String userId) {
        List<Activity> activities = Lists.newArrayList();

        for (Map.Entry<String, List<LoanEvent>> entry: loanEventsByCredentials.entrySet()) {
            String credentials = entry.getKey();
            List<LoanEvent> credentialEvents = activityHelper.uniqueLoanEventsByAccountsOnDate(entry.getValue());

            activities.add(createActivityByCredentials(credentials, credentialEvents, userId));
        }

        return activities;
    }

    /**
     * Creates a single activity based on the credentials and it's corresponding loan events
     */
    private Activity createActivityByCredentials(String credentials, List<LoanEvent> credentialEvents, String userId){

        Double weightedInterestChangeAverage = LoanUtils.interestRateChangeWeightedAverage(credentialEvents);
        Double weightedInterestAverage = LoanUtils.interestWeightedAverage(credentialEvents);

        String activityType = activityHelper.getActivityTypeByValue(weightedInterestChangeAverage);
        String title = activityHelper
                .getActivityTitleByValueAndPlural(weightedInterestChangeAverage, credentialEvents.size());
        Loan.Type loanType = Loan.Type.MORTGAGE;

        LoanEventActivityData data = new LoanEventActivityData();
        credentialEvents = activityHelper.transformProvidersToDisplayName(credentialEvents);
        String message = credentialEvents.get(0).getProvider();
        data.setLoanEvents(credentialEvents);
        data.setInterestRateChange(weightedInterestChangeAverage);
        data.setCurrentInterestRate(weightedInterestAverage);
        data.setBalance(credentialEvents.stream().mapToDouble(LoanEvent::getBalance).sum());
        data.setLoanType(loanType.toString());
        data.setChallengeLoanEligible(activityHelper.eligibleForSwitchMortgage());

        Date date = credentialEvents.get(0).getTimestamp();
        String key = LoanUtils.generateNotificationKey(Activity.Types.LOAN + "." + credentials, date);
        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

        return createActivity(
                userId,
                date,
                activityType,
                title,
                message,
                data,
                key,
                feedActivityIdentifier
        );
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }

    @Override
    protected List<Notification> createNotifications(Activity activity, ActivityGeneratorContext context){


        Notification notification = new Notification(activity.getUserId());

        notification.setDate(activity.getDate());
        notification.setSensitiveMessage(activity.getSensitiveMessage());
        notification.setMessage(activity.getMessage());
        notification.setKey(activity.getKey());
        notification.setType(activity.getType());
        notification.setTitle(activity.getTitle());
        notification.setGroupable(false);

        notification.setUrl(
                deepLinkBuilderFactory.createApplication(ApplicationType.SWITCH_MORTGAGE_PROVIDER.toString()).build()
        );

        return Lists.newArrayList(notification);
    }

    @Override
    public List<Notification> generateNotifications(Activity activity, ActivityGeneratorContext context) {
        return super.generateNotifications(activity, context);
    }
}
