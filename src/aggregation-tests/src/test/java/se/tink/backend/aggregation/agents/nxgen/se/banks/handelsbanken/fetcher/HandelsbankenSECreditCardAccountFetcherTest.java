package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEAuthenticatedTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.HandelsbankenCreditCardAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.utils.IsNot0Matcher.isNot0;

public class HandelsbankenSECreditCardAccountFetcherTest extends HandelsbankenSEAuthenticatedTest {

    @Test
    public void fetchingCreditCards() throws Exception {
        autoAuthenticator.autoAuthenticate();

        Collection<CreditCardAccount> accounts = new HandelsbankenCreditCardAccountFetcher(client, sessionStorage).fetchAccounts();
        assertThat(accounts, notNullValue());
        assertFalse(accounts.isEmpty());
        accounts.forEach(account -> {
            assertThat(account.getType(), is(AccountTypes.CREDIT_CARD));
            assertThat(account.getBalance().getValue(), isNot0());
            assertThat(account.getAvailableCredit().getValue(), isNot0());
        });
    }

}
