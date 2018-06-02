package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIAuthenticatedTest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFITestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.HandelsbankenCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.HandelsbankenCreditCardTransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HandelsbankenFICreditCardTransactionFetcherTest extends HandelsbankenFIAuthenticatedTest {

    @Test
    public void accountsAreFetched() throws Exception {
        autoAuthenticator.autoAuthenticate();

        HandelsbankenCreditCardTransactionFetcher creditCardTransactionFetcher =
                new HandelsbankenCreditCardTransactionFetcher(client, sessionStorage);

        Collection<CreditCardAccount> accounts = new HandelsbankenCreditCardAccountFetcher(client, sessionStorage)
                .fetchAccounts();

        assertThat(accounts, notNullValue());
        assertTrue(accounts.isEmpty());// As long as the test account doesn't have any credit cards...
//        assertFalse(accounts.isEmpty());
//        accounts.forEach(account -> {
//            List<Transaction> transactions = creditCardTransactionFetcher
//                    .fetchTransactionsFor(account);
//            assertThat(transactions, notNullValue());
//            assertFalse(transactions.isEmpty());
//            transactions.forEach(transaction -> {
//                assertNotEquals("Transaction: " + transaction + " does not have amount", transaction.getAmount(), 0d);
//                assertFalse("Transaction: " + transaction + " does not have description", isBlank(transaction
//                        .getDescription()));
//
//            });
//        });
    }

    @Override
    protected HandelsbankenFITestConfig getTestConfig() {
        return HandelsbankenFITestConfig.USER_1;
    }
}
