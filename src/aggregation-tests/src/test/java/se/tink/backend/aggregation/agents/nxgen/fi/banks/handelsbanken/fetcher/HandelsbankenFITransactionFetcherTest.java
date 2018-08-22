package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher;

import java.util.Collection;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIAuthenticatedTest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFITestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.utils.IsNot0Matcher.isNot0;

public class HandelsbankenFITransactionFetcherTest extends HandelsbankenFIAuthenticatedTest {

    @Test
    public void itWorks() throws Exception {
        autoAuthenticator.autoAuthenticate();

        Collection<TransactionalAccount> accounts = new HandelsbankenTransactionalAccountFetcher(client, sessionStorage).fetchAccounts();
        accounts.forEach(account -> {
            List<AggregationTransaction> transactions = new HandelsbankenAccountTransactionFetcher(client, sessionStorage)
                    .fetchTransactionsFor(account);
            assertThat(transactions, notNullValue());
            assertFalse(transactions.isEmpty());
            transactions.forEach(transaction -> {
                assertThat("Transaction: " + transaction + " does not have amount",
                        transaction.getAmount().getValue(), isNot0());
                assertFalse("Transaction: " + transaction + " does not have description", isBlank(transaction
                        .getDescription()));
            });
        });
    }

    @Override
    protected HandelsbankenFITestConfig getTestConfig() {
        return HandelsbankenFITestConfig.USER_1;
    }
}
