package se.tink.backend.system.workers.processor.other;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.system.guice.SystemTestModuleFactory;
import se.tink.backend.system.guice.TestRepositoriesModule;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.util.GuiceModules;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(GuiceRunner.class)
public class RemoveRedundantTransactionsCommandTest {
    @Inject
    private TestProcessor transactionProcessor;

    @Inject
    private TestUtil testUtil;

    private List<User> users;
    private Map<String, Credentials> credentials;
    private Map<String, UserData> userDatas;

    @Before
    public void setup() {
        users = testUtil.getTestUsers("RemoveRedundantTransactionsCommandTest");
        this.credentials = users.stream().map(user -> testUtil.getCredentials(user, "swedbank-bankid"))
                .collect(Collectors.toMap(c -> c.getUserId(), c -> c));

        this.userDatas = users.stream().map((user -> {
            UserData userData = new UserData();
            userData.setUser(user);
            userData.setCredentials(com.google.common.collect.Lists.newArrayList(credentials.get(user.getId())));
            return userData;
        })).collect(Collectors.toMap(ud -> ud.getUser().getId(), ud -> ud));
    }

    /**
     * expectedNumberOfTransactionsToBeRemoved is the number of transaction that is redundant.
     * It will take the oldest transaction, add five days to that date, and try to remove all
     * transactions after that date.
     */
    public void testRemoveRedundantTransactions(List<Transaction> transactions,
                                                int expectedNumberOfTransactionsToBeRemoved, User user) throws InterruptedException {
        UserData userData = userDatas.get(user.getId());
        userData.setTransactions(transactions);

        TransactionProcessorContext context = new TransactionProcessorContext(
                user,
                testUtil.getProvidersByName(),
                transactions,
                userData,
                credentials.get(user.getId()).getId()
        );

        transactionProcessor.process(context, userData);

        int expNumberOfTransactionsLeft = transactions.size() - expectedNumberOfTransactionsToBeRemoved;

        Assert.assertEquals(expNumberOfTransactionsLeft, context.getUserData().getInStoreTransactions().size());
    }

    @Test
    public void testToRemoveRedundantTransactions1() {
        users.forEach(user -> {
            List<Transaction> transactions = Lists.newArrayList(
                    testUtil.getNewTransaction(user.getId(), -95, "Pontus", "02", "01"),
                    testUtil.getNewTransaction(user.getId(), -195, "Pontus", "02", "02"),
                    testUtil.getNewTransaction(user.getId(), -295, "Pontus", "02", "03"),
                    testUtil.getNewTransaction(user.getId(), -395, "Pontus", "02", "04"),
                    testUtil.getNewTransaction(user.getId(), -495, "Pontus", "01", "01"),
                    testUtil.getNewTransaction(user.getId(), -595, "Pontus", "01", "02"));

            try {
                testRemoveRedundantTransactions(transactions, 2, user); // transaction 5 and 6 should be redundant in this case.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void testToRemoveRedundantTransactions2() {
        users.forEach(user -> {
            List<Transaction> transactions = Lists.newArrayList(
                    testUtil.getNewTransaction(user.getId(), -95, "Pontus", "02", "01"),
                    testUtil.getNewTransaction(user.getId(), -195, "Pontus", "02", "02"),
                    testUtil.getNewTransaction(user.getId(), -295, "Pontus", "02", "03"),
                    testUtil.getNewTransaction(user.getId(), -395, "Pontus", "02", "04"),
                    testUtil.getNewTransaction(user.getId(), -495, "Pontus", "02", "05"),
                    testUtil.getNewTransaction(user.getId(), -595, "Pontus", "02", "05"));

            try {
                testRemoveRedundantTransactions(transactions, 0, user);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void testToRemoveRedundantTransactions3() {
        users.forEach(user -> {
            List<Transaction> transactions = Lists.newArrayList(
                    testUtil.getNewTransaction(user.getId(), -95, "Pontus", "02", "01"),
                    testUtil.getNewTransaction(user.getId(), -195, "Pontus", "02", "01"),
                    testUtil.getNewTransaction(user.getId(), -295, "Pontus", "02", "01"),
                    testUtil.getNewTransaction(user.getId(), -395, "Pontus", "02", "01"),
                    testUtil.getNewTransaction(user.getId(), -495, "Pontus", "02", "01"),
                    testUtil.getNewTransaction(user.getId(), -595, "Pontus", "02", "01"));

            try {
                testRemoveRedundantTransactions(transactions, 0, user);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void testToRemoveRedundantTransactions4() {
        users.forEach(user -> {
            List<Transaction> transactions = Lists.newArrayList(
                    testUtil.getNewTransaction(user.getId(), -95, "Pontus", "02", "01"),
                    testUtil.getNewTransaction(user.getId(), -195, "Pontus", "01", "02"),
                    testUtil.getNewTransaction(user.getId(), -295, "Pontus", "01", "03"),
                    testUtil.getNewTransaction(user.getId(), -395, "Pontus", "01", "04"),
                    testUtil.getNewTransaction(user.getId(), -495, "Pontus", "01", "01"),
                    testUtil.getNewTransaction(user.getId(), -595, "Pontus", "01", "02"));

            try {
                testRemoveRedundantTransactions(transactions,
                        5, user); // transaction 2, 3, 4, 5 and 6 should be redundant in this case.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
