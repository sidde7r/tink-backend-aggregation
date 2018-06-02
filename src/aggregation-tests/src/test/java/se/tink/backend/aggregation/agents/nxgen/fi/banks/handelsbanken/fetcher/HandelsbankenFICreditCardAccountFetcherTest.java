package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIAuthenticatedTest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFITestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.HandelsbankenCreditCardAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HandelsbankenFICreditCardAccountFetcherTest extends HandelsbankenFIAuthenticatedTest {

    @Test
    public void fetchingCreditCards() throws Exception {
        autoAuthenticator.autoAuthenticate();

        Collection<CreditCardAccount> accounts = new HandelsbankenCreditCardAccountFetcher(client, sessionStorage).fetchAccounts();
        assertThat(accounts, notNullValue());
        assertTrue(accounts.isEmpty());// As long as the test account doesn't have any credit cards...
//        assertFalse(accounts.isEmpty());
//        accounts.forEach(account -> {
//            assertThat(account.getType(), is(AccountTypes.CREDIT_CARD));
//            assertThat(account.getBalance(), IsNot0Matcher.isNot0());
//            assertThat(account.getAvailableCredit(), IsNot0Matcher.isNot0());
//        });
    }

    @Override
    protected HandelsbankenFITestConfig getTestConfig() {
        return HandelsbankenFITestConfig.USER_1;
    }
}
