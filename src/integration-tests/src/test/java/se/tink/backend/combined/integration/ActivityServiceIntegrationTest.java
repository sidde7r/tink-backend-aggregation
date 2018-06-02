package se.tink.backend.combined.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import java.util.List;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.core.Activity;
import se.tink.backend.core.User;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.rpc.ActivityQuery;

/**
 * TODO this is a unit test
 */
public class ActivityServiceIntegrationTest extends AbstractServiceIntegrationTest {

    protected static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDoubleChargeNotGeneratedForNoDoubleCharges() throws Exception {

        User statisticTestSessionId = registerTestUserWithDemoCredentialsAndData("anv21");

        List<Activity> activities = serviceFactory.getActivityService().query(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, statisticTestSessionId), new ActivityQuery()).getActivities();

        ImmutableListMultimap<String, Activity> activitiesByType = Multimaps.index(activities,
                Activity::getType);

        List<Activity> doubleChargeActivities = activitiesByType.get(Activity.Types.DOUBLE_CHARGE);

        // there are no double charges for this account

        Assert.assertEquals(0, doubleChargeActivities.size());

        deleteUser(statisticTestSessionId);
    }

    @Test
    public void testNoDoubleChargeForDifferentDaysAndDifferentAmounts() throws Exception {

        User statisticTestSessionId = registerTestUserWithDemoCredentialsAndData("anv28");

        List<Activity> activities = serviceFactory.getActivityService().query(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, statisticTestSessionId), new ActivityQuery()).getActivities();

        ImmutableListMultimap<String, Activity> activitiesByType = Multimaps.index(activities,
                Activity::getType);

        List<Activity> doubleChargeActivities = activitiesByType.get(Activity.Types.DOUBLE_CHARGE);

        // there are 7 potential double charge not they are either to low, a difference is the amounts or not the same
        // day

        Assert.assertEquals(0, doubleChargeActivities.size());

        deleteUser(statisticTestSessionId);
    }

    /**
     * Verifies that we don't generate large expense activities for historical
     * steady behaviours. In this case a large rent transaction every month.
     */
    @Test
    public void testLargeExpenseNotGeneratedForSteadyBehavior() throws Exception {

        User statisticTestSessionId = registerTestUserWithDemoCredentialsAndData("anv23");

        List<Activity> activities = serviceFactory.getActivityService().query(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, statisticTestSessionId), new ActivityQuery()).getActivities();

        ImmutableListMultimap<String, Activity> activitiesByType = Multimaps.index(activities,
                Activity::getType);

        List<Activity> largeExpenseActivities = activitiesByType.get(Activity.Types.LARGE_EXPENSE);

        // Print out the large expenses
        for(Activity act : largeExpenseActivities){
            System.out.println(act.getMessage() + act.getContent());
        }

        Assert.assertEquals(0, largeExpenseActivities.size());

        deleteUser(statisticTestSessionId);

    }

    /**
     * Verifies that we don't generate large expense activities for historical
     * slowly increasing behaviours. In this case a rent that slowly is increasing every month.
     */
    @Test
    public void testLargeExpenseNotGeneratedForSlowlyIncreasingBehavior() throws Exception {

        User statisticTestSessionId = registerTestUserWithDemoCredentialsAndData("anv26");

        List<Activity> activities = serviceFactory.getActivityService().query(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, statisticTestSessionId), new ActivityQuery()).getActivities();

        ImmutableListMultimap<String, Activity> activitiesByType = Multimaps.index(activities,
                Activity::getType);

        List<Activity> largeExpenseActivities = activitiesByType.get(Activity.Types.LARGE_EXPENSE);

        // Print out the large expenses
        for(Activity act : largeExpenseActivities){
            System.out.println(act.getMessage() + act.getContent());
        }

        Assert.assertEquals(0, largeExpenseActivities.size());

        deleteUser(statisticTestSessionId);

    }

    private List<Activity> waitActivities(User user, boolean fail) throws InterruptedException {
        List<Activity> activities = Lists.newArrayList();

        for (int i = 0; i < 10 && activities.size() == 0; ++i) {
            activities = serviceFactory.getActivityService().query(new AuthenticatedUser(
                    HttpAuthenticationMethod.BASIC, user), new ActivityQuery()).getActivities();
            Thread.sleep(1000);
        }

        if (fail && activities.size() == 0) {
            Assert.fail("Could not generate activities");
        }

        return activities;
    }
}
