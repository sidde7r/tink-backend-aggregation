package se.tink.backend.common.workers.activity.generators;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.common.ActivityGeneratorWorkerTestBase;
import se.tink.backend.categorization.api.SECategories;
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
public class DoubleChargeActivityGeneratorTest extends ActivityGeneratorWorkerTestBase {

    private static final TypeReference<List<Transaction>> TRANSACTION_LIST_TYPE_REFERENCE = new TypeReference<List<Transaction>>() {
    };

    @Test
    public void testCorrectNumberOfActivities() {
        getActivityGeneratorContext().forEach(context -> {

            DoubleChargeActivityGenerator generator = new DoubleChargeActivityGenerator(new DeepLinkBuilderFactory(""));

            generator.generateActivity(context);

            List<Activity> activities = context.getActivities();

            int expNumberOfActivities = 1;
            int actualNumberOfActivities = 0;

            if (activities != null) {
                actualNumberOfActivities = activities.size();
            }

            Assert.assertEquals(expNumberOfActivities, actualNumberOfActivities);
        });
    }

    @Test
    public void testNoActivityOnMortgageCategory() {
        getActivityGeneratorContext().forEach(context -> {
            DoubleChargeActivityGenerator generator = new DoubleChargeActivityGenerator(new DeepLinkBuilderFactory(""));

            generator.generateActivity(context);

            List<Activity> activities = context.getActivities();

            Activity doubleChargeActivity = activities.get(0);
            final List<Transaction> doubleChargeTransaction = doubleChargeActivity
                    .getContent(TRANSACTION_LIST_TYPE_REFERENCE);
            final Iterable<String> doubleChargeTransactionIds = Iterables.transform(doubleChargeTransaction,
                    Transaction::getId);

            // Find duplicate transactions in context.

            Iterable<Transaction> doubleChargeTransactions = Iterables.filter(context.getTransactions(),
                    t -> Iterables.contains(doubleChargeTransactionIds, t.getId()));


            Assert.assertFalse("Did not find transaction in context.", Iterables.size(doubleChargeTransactions) == 0);

            // Recategorize as mortgage

            for (Transaction transaction : doubleChargeTransactions) {
                transaction
                        .setCategory(context.getCategoriesByCodeForLocale().get(SECategories.Codes.EXPENSES_HOME_MORTGAGE));
            }

            // Reset transactions on context.
            context = getActivityGeneratorContext().get(0);
            Iterable<Transaction> allTransactions = Iterables.concat(context.getTransactions(), doubleChargeTransactions);
            context.setTransactions(Lists.newArrayList(allTransactions));
            List<Activity> activities2 = context.getActivities();
            Assert.assertEquals(0, activities2.size());
        });
    }

    @Test
    public void testNotificationUrlSebCluster() {
        getActivityGeneratorContext().forEach(context -> {
            Activity activity = mockData(context);

            serviceContext.getConfiguration().setCluster(Cluster.CORNWALL);

            DoubleChargeActivityGenerator generator = new DoubleChargeActivityGenerator(new DeepLinkBuilderFactory("seb://tink/"));

            List<Notification> notifications = generator.createNotifications(activity, context);

            Assert.assertEquals(1, notifications.size());
            Assert.assertEquals("seb://tink/account/accountId?transaction1=transactionId1&transaction2=transactionId2",
                    notifications.get(0).getUrl());
        });
    }

    @Test
    public void testNotificationUrlSebCluster_noAccount() {
        getActivityGeneratorContext().forEach(context -> {
            Activity activity = mockData(context);

            context.setAccounts(Lists.<Account>newArrayList());

            serviceContext.getConfiguration().setCluster(Cluster.CORNWALL);

            DoubleChargeActivityGenerator generator = new DoubleChargeActivityGenerator(new DeepLinkBuilderFactory("seb://tink/"));

            List<Notification> notifications = generator.createNotifications(activity, context);

            Assert.assertEquals(1, notifications.size());
            Assert.assertEquals("seb://tink/", notifications.get(0).getUrl());
        });
    }

    @Test
    public void testNotificationUrlTinkCluster() {
        getActivityGeneratorContext().forEach(context -> {
            Activity activity = mockData(context);

            serviceContext.getConfiguration().setCluster(Cluster.TINK);

            DoubleChargeActivityGenerator generator = new DoubleChargeActivityGenerator(new DeepLinkBuilderFactory("tink://"));

            List<Notification> notifications = generator.createNotifications(activity, context);

            Assert.assertEquals(1, notifications.size());
            Assert.assertEquals("tink://transactions/transactionId1",
                    notifications.get(0).getUrl());
        });
    }

    private Activity mockData(ActivityGeneratorContext context) {
        context.setServiceContext(serviceContext);

        Transaction transaction1 = new Transaction();
        transaction1.setId("transactionId1");
        transaction1.setAccountId("accountId");

        Transaction transaction2 = new Transaction();
        transaction2.setId("transactionId2");
        transaction2.setAccountId("accountId");

        Account account = new Account();
        account.setId("accountId");

        context.setAccounts(Collections.singletonList(account));

        Activity activity = new Activity();
        activity.setUserId("userId");
        activity.setDate(new Date());
        activity.setContent(Arrays.asList(transaction1, transaction2));
        activity.setType(Activity.Types.DOUBLE_CHARGE);
        activity.setTitle("Activity title");
        return activity;
    }
}
