package se.tink.backend.combined.integration;

import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticQuery;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

/**
 * TODO this is a unit test
 */
public class AccountServiceIntegrationTest extends AbstractServiceIntegrationTest {

	@Test
    public void testOwnershipSplit() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData();

        List<Account> as1 = serviceFactory.getAccountService().listAccounts(user).getAccounts();

        StatisticQuery query = new StatisticQuery();
        query.setTypes(Collections.singletonList("expenses-by-category"));
        query.setResolution(ResolutionTypes.MONTHLY);

        double v1 = 0;

        List<Statistic> ss1 = serviceFactory.getStatisticsService().query(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), query);

        for (Statistic s : ss1) {
            v1 += s.getValue();
        }

        for (Account a : as1) {
            a.setOwnership(0.5);
            serviceFactory.getAccountService().update(user, a.getId(), a);
        }

        List<Account> as2 = serviceFactory.getAccountService().listAccounts(user).getAccounts();

        for (Account a : as2) {
            Assert.assertEquals(a.getOwnership(), 0.5d, 0);
        }

        double v2 = 0;

        List<Statistic> ss2 = serviceFactory.getStatisticsService().query(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), query);

        for (Statistic s : ss2) {
            v2 += s.getValue();
        }

        Assert.assertEquals(v1 * 0.5, v2, 0.0001);
    }

    @Ignore
    @Test
    public void testModifyAccountAndRefresh() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData();

        List<Credentials> cs1 = serviceFactory.getCredentialsService().list(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user));
        Credentials c1 = cs1.get(0);

        List<Account> as1 = serviceFactory.getAccountService().listAccounts(user).getAccounts();
        Assert.assertEquals(1, as1.size());

        List<Transaction> ts1 = serviceFactory.getTransactionService().list(user, null, null, null, 0, 0, null, null);
        Assert.assertThat(ts1.size(), is(not(0)));

        List<Statistic> ss1 = serviceFactory.getStatisticsService().list(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user));
        Assert.assertThat(ss1.size(), is(not(0)));

        Account a1 = as1.get(0);
        a1.setExcluded(!a1.isExcluded());
        a1.setName("test name1");
        a1.setType(AccountTypes.PENSION);

        serviceFactory.getAccountService().update(user, a1.getId(), a1);

        Thread.sleep(5000); // to be sure update is complete

        serviceFactory.getCredentialsService().refresh(
                new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user),
                c1.getId(),
                Collections.emptySet()
        );

        Thread.sleep(1000);

        waitForRefresh(user);

        List<Account> as2 = serviceFactory.getAccountService().listAccounts(user).getAccounts();
        Assert.assertEquals(as1.size(), as2.size());

        Account a2 = as2.get(0);

        Assert.assertTrue(a1.isFavored() == a2.isFavored());

        Assert.assertEquals(a1.getName(), a2.getName());
        Assert.assertEquals(a1.getType(), a2.getType());

        Assert.assertTrue(a2.isUserModifiedName());
        Assert.assertTrue(a2.isUserModifiedType());

        deleteUser(user);
    }
}
