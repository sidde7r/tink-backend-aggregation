package se.tink.backend.common.workers.activity.generators;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.common.ActivityGeneratorWorkerTestBase;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.AccountBalanceActivityData;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Notification;
import se.tink.libraries.cluster.Cluster;

/**
 * TODO this is a unit test
 */
public class AccountBalanceActivityGeneratorTest extends ActivityGeneratorWorkerTestBase {

    @Test
    public void testNotificationUrlSebCluster() {
        getActivityGeneratorContext().forEach(context -> {
                Activity activity = mockData(context);

        context.setCluster(Cluster.CORNWALL);
        serviceContext.getConfiguration().getNotifications().setDeepLinkPrefix("seb://tink");

        AccountBalanceActivityGenerator generator = new AccountBalanceActivityGenerator(
                new DeepLinkBuilderFactory("seb://tink/"));

        List<Notification> notifications = generator.createNotifications(activity, context);

        Assert.assertEquals(1, notifications.size());
        Assert.assertEquals("seb://tink/account/accountId",
                notifications.get(0).getUrl());
        });
    }

    @Test
    public void testNotificationUrlTinkCluster() {
        getActivityGeneratorContext().forEach(context -> {
            Activity activity = mockData(context);

            context.setCluster(Cluster.TINK);

            AccountBalanceActivityGenerator generator = new AccountBalanceActivityGenerator(new DeepLinkBuilderFactory("tink://"));

            List<Notification> notifications = generator.createNotifications(activity, context);

            Assert.assertEquals(1, notifications.size());
            Assert.assertEquals("tink://accounts/accountId", notifications.get(0).getUrl());
        });
    }

    private Activity mockData(ActivityGeneratorContext context) {
        context.setServiceContext(serviceContext);

        Account account = new Account();
        account.setId("accountId");

        context.setAccounts(Collections.singletonList(account));

        AccountBalanceActivityData data = new AccountBalanceActivityData();
        data.setAccount(account);

        Activity activity = new Activity();
        activity.setUserId("userId");
        activity.setDate(new Date());
        activity.setContent(data);
        activity.setType(Activity.Types.BALANCE_LOW);
        activity.setTitle("Activity title");

        return activity;
    }
}
