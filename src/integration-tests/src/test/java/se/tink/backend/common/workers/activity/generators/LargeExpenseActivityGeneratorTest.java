package se.tink.backend.common.workers.activity.generators;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.common.ActivityGeneratorWorkerTestBase;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Notification;
import se.tink.backend.core.Transaction;
import se.tink.libraries.cluster.Cluster;

/**
 * TODO this is a unit test
 */
public class LargeExpenseActivityGeneratorTest extends ActivityGeneratorWorkerTestBase {

    @Test
    public void testNotificationUrlSebCluster() {
        getActivityGeneratorContext().forEach(context -> {
            Activity activity = mockData(context);

            serviceContext.getConfiguration().setCluster(Cluster.CORNWALL);

            LargeExpenseActivityGenerator generator = new LargeExpenseActivityGenerator(new DeepLinkBuilderFactory("seb://tink/"));

            List<Notification> notifications = generator.createNotifications(activity, context);

            Assert.assertEquals(1, notifications.size());
            Assert.assertEquals("seb://tink/account/accountId/transaction/transactionId1", notifications.get(0).getUrl());
        });
    }

    @Test
    public void testNotificationUrlTinkCluster() {
        getActivityGeneratorContext().forEach(context -> {
            Activity activity = mockData(context);

            serviceContext.getConfiguration().setCluster(Cluster.TINK);

            LargeExpenseActivityGenerator generator = new LargeExpenseActivityGenerator(new DeepLinkBuilderFactory("tink://"));

            List<Notification> notifications = generator.createNotifications(activity, context);

            Assert.assertEquals(1, notifications.size());
            Assert.assertEquals("tink://transactions/transactionId1", notifications.get(0).getUrl());
        });
    }

    private Activity mockData(ActivityGeneratorContext context) {
        context.setServiceContext(serviceContext);

        Transaction transaction1 = new Transaction();
        transaction1.setId("transactionId1");
        transaction1.setAccountId("accountId");

        Account account = new Account();
        account.setId("accountId");

        context.setAccounts(Collections.singletonList(account));

        Activity activity = new Activity();
        activity.setUserId("userId");
        activity.setDate(new Date());
        activity.setContent(transaction1);
        activity.setType(Activity.Types.LARGE_EXPENSE);
        activity.setTitle("Activity title");
        return activity;
    }
}
