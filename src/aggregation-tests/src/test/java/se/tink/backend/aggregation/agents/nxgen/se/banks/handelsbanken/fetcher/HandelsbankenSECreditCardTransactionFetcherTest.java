package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher;

import java.util.Collection;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEAuthenticatedTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.HandelsbankenCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.HandelsbankenCreditCardTransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

public class HandelsbankenSECreditCardTransactionFetcherTest extends HandelsbankenSEAuthenticatedTest {

    @Test
    public void accountsAreFetched() throws Exception {
        autoAuthenticator.autoAuthenticate();

        HandelsbankenCreditCardTransactionFetcher creditCardTransactionFetcher =
                new HandelsbankenCreditCardTransactionFetcher(client, sessionStorage);

        Collection<CreditCardAccount> accounts = new HandelsbankenCreditCardAccountFetcher(client, sessionStorage)
                .fetchAccounts();

        assertThat(accounts, notNullValue());
        assertFalse(accounts.isEmpty());
        accounts.forEach(account -> {
            List<AggregationTransaction> transactions = creditCardTransactionFetcher
                    .fetchTransactionsFor(account);
            assertThat(transactions, notNullValue());
            assertFalse(transactions.isEmpty());
            transactions.forEach(transaction -> {
                assertNotEquals("Transaction: " + transaction + " does not have amount", transaction.getAmount().getValue(), 0d);
                assertFalse("Transaction: " + transaction + " does not have description", isBlank(transaction
                        .getDescription()));
            });
        });
    }
}
