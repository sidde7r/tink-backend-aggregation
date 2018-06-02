package se.tink.backend.combined.integration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.OrderTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionQuery;
import se.tink.backend.core.TransactionSortTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.ResolutionTypes;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransactionServiceIntegrationTest extends AbstractServiceIntegrationTest {
    StatisticDao statisticDao;

    @Before
    public void setUp() {
        statisticDao = serviceContext.getDao(StatisticDao.class);
        user = null;
    }

    protected User user;

    @After
    public void tearDown() throws Exception {
        if (user != null) {
            deleteUser(user);
        }
    }

    @Test
    public void testListTransactionByPeriod() throws Exception {
        user = registerTestUserWithDemoCredentialsAndData("anv1ud");
        List<Transaction> transactions1 = serviceFactory.getTransactionService()
                .list(user, null, null, Lists.newArrayList("2012-10"), 0,
                        0, null, null);

        for (Transaction t : transactions1) {
            Assert.assertEquals("2012-10",
                    UserProfile.ProfileDateUtils.getMonthPeriod(t.getDate(), user.getProfile()));
        }

        List<Transaction> transactions2 = serviceFactory.getTransactionService()
                .list(user, null, null, Lists.newArrayList("2012-11"), 0,
                        0, null, null);

        for (Transaction t : transactions2) {
            Assert.assertEquals("2012-11",
                    UserProfile.ProfileDateUtils.getMonthPeriod(t.getDate(), user.getProfile()));
        }
    }

    @Test
    public void testQueryTransactionByRootCategoriesAccount() throws Exception {
        user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        List<Category> categories = serviceFactory.getCategoryService().list(user, null);

        List<Category> rootCategories = Lists.newArrayList(Iterables.filter(categories,
                category -> (category.getParent() == null)));

        TransactionQuery query1 = new TransactionQuery();
        List<Transaction> transactions1 = serviceFactory.getTransactionService().query(user, query1).getTransactions();

        TransactionQuery query2 = new TransactionQuery();
        query2.setCategories(Category.getCategoryIds(rootCategories));

        List<Transaction> transactions2 = serviceFactory.getTransactionService().query(user, query2).getTransactions();

        Assert.assertEquals(transactions1.size(), transactions2.size());
    }

    @Test
    public void testQueryTransactionByPeriodMonthly() throws Exception {
        user = registerTestUserWithDemoCredentialsAndData("201212121212");

        String currentPeriod = se.tink.libraries.date.DateUtils.getCurrentMonthPeriod();

        TransactionQuery query1 = new TransactionQuery();
        query1.setPeriods(Lists.newArrayList(currentPeriod));
        query1.setResolution(ResolutionTypes.MONTHLY);

        List<Transaction> transactions1 = serviceFactory.getTransactionService().query(user, query1).getTransactions();

        for (Transaction t : transactions1) {
            String transactionMonthPeriod = se.tink.libraries.date.DateUtils.getMonthPeriod(t.getDate());
            Assert.assertEquals(currentPeriod, transactionMonthPeriod);
        }
    }

    @Test
    public void testSortTransactions() throws Exception {
        user = registerTestUserWithDemoCredentialsAndData("anv1ud");

        testSortTransactions(user, TransactionSortTypes.DESCRIPTION,
                (t1, t2) -> t1.getDescription().compareToIgnoreCase(t2.getDescription()));

        testSortTransactions(user, TransactionSortTypes.DATE, (t1, t2) -> {
            int dateComparison = Longs.compare(t1.getDate().getTime(), t2.getDate().getTime());

            if (dateComparison == 0) {
                dateComparison = Longs.compare(t1.getInserted(), t2.getInserted());
            }

            return dateComparison;
        });

        testSortTransactions(user, TransactionSortTypes.AMOUNT,
                (t1, t2) -> Doubles.compare(t1.getAmount(), t2.getAmount()));

        final ImmutableMap<String, Account> accountsById = Maps
                .uniqueIndex(serviceFactory.getAccountService().listAccounts(user).getAccounts(),
                        Account::getId);

        testSortTransactions(user, TransactionSortTypes.ACCOUNT,
                (t1, t2) -> accountsById.get(t1.getAccountId()).getName()
                        .compareToIgnoreCase(accountsById.get(t2.getAccountId()).getName()));

        final ImmutableMap<String, Category> categoriesById = Maps
                .uniqueIndex(serviceFactory.getCategoryService().list(user, null),
                        Category::getId);

        testSortTransactions(user, TransactionSortTypes.CATEGORY,
                (t1, t2) -> categoriesById.get(t1.getCategoryId()).getDisplayName()
                        .compareToIgnoreCase(categoriesById.get(t2.getCategoryId()).getDisplayName()));
    }

    private void testSortTransactions(User user, TransactionSortTypes sort, Comparator<Transaction> comparator) {
        TransactionQuery q1 = new TransactionQuery();

        q1.setLimit(100000);
        q1.setSort(sort);
        q1.setOrder(OrderTypes.DESC);

        List<Transaction> ts1 = serviceFactory.getTransactionService().query(user, q1).getTransactions();

        // Test the DESC sorting.

        if (comparator != null) {
            for (int i = 1; i < ts1.size(); i++) {
                Transaction t1 = ts1.get(i - 1);
                Transaction t2 = ts1.get(i);

                int comparision = comparator.compare(t1, t2);

                Assert.assertTrue(comparision >= 0);
            }
        }

        TransactionQuery q2 = new TransactionQuery();
        q2.setLimit(100000);
        q2.setSort(sort);
        q2.setOrder(OrderTypes.ASC);

        List<Transaction> ts2 = serviceFactory.getTransactionService().query(user, q2).getTransactions();

        // Test the ASC sorting.

        if (comparator != null) {
            for (int i = 1; i < ts2.size(); i++) {
                Transaction t1 = ts2.get(i - 1);
                Transaction t2 = ts2.get(i);

                int comparision = comparator.compare(t1, t2);

                Assert.assertTrue(comparision <= 0);
            }
        }

        // Check that they match in reverse order.

        Collections.reverse(ts2);

        Assert.assertEquals(ts1.size(), ts2.size());

        for (int i = 0; i < ts1.size(); i++) {
            Transaction t1 = ts1.get(i);
            Transaction t2 = ts2.get(i);

            // System.out.println(i + " : " + t1.getDescription() + " - " +
            // t2.getDescription());

            Assert.assertEquals(t1.getId(), t2.getId());
        }
    }
}
