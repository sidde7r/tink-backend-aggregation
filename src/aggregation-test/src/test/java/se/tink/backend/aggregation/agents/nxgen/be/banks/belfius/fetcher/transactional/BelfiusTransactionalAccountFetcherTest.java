package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusUpcomingTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchUpcomingTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.agents.rpc.User;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class BelfiusTransactionalAccountFetcherTest extends BelfiusTest {

    @Test
    public void canFetchAccountsAndTransactions() throws Exception {
        autoAuthenticate();

        BelfiusTransactionalAccountFetcher fetcher = new BelfiusTransactionalAccountFetcher(this.apiClient);

        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());

        accounts.forEach(account -> {
            Collection<? extends Transaction> transactions = fetcher.fetchTransactionsFor(account)
                    .getTinkTransactions();
            assertNotNull(transactions);
            assertFalse(transactions.isEmpty());
        });
    }


    @Test
    public void parseResponseTest() {
        FetchProductsResponse fetchProductsResponse = SerializationUtils
                .deserializeFromString(ProductList.bigProductList, FetchProductsResponse.class);

        List<TransactionalAccount> collect = fetchProductsResponse.stream()
                .filter(entry -> entry.getValue().isTransactionalAccount())
                .map(entry -> entry.getValue().toTransactionalAccount(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        assertTrue(collect.get(0).getBalance().getValue() == 0);
        assertTrue(collect.get(1).getBalance().getValue() == 847.24);
        assertTrue(collect.get(2).getBalance().getValue() == 0);
        assertTrue(collect.get(3).getBalance().getValue() == 0.01);
        assertTrue(collect.get(4).getBalance().getValue() == 0);
        assertTrue(collect.get(5).getBalance().getValue() == 38374.26);
    }

    @Test
    public void parseTransactionsTest() {
        FetchTransactionsResponse belfiusTransaction = SerializationUtils
                .deserializeFromString(ProductList.transactions, FetchTransactionsResponse.class);

        List<Transaction> collect = belfiusTransaction.stream().map(BelfiusTransaction::toTinkTransaction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        assertTrue(collect.size() == 8);
        assertTrue(collect.get(0).toSystemTransaction(new User()).getAmount() == -5510.08);
        assertTrue(collect.get(1).toSystemTransaction(new User()).getAmount() == -5123510.08);
        assertTrue(collect.get(2).toSystemTransaction(new User()).getAmount() == 5510.08);
        assertTrue(collect.get(3).toSystemTransaction(new User()).getAmount() == 5123510.08);
        assertTrue(collect.get(4).toSystemTransaction(new User()).getAmount() == -103.6);
        assertTrue(collect.get(5).toSystemTransaction(new User()).getAmount() == 103.6);
        assertTrue(collect.get(6).toSystemTransaction(new User()).getAmount() == 103.6012);
        assertTrue(collect.get(7).toSystemTransaction(new User()).getAmount() == -0.35);
    }

    @Test
    public void parseUpcomingTransactionsTest() {
        FetchUpcomingTransactionsResponse belfiusUpcomingTransaction = SerializationUtils
                .deserializeFromString(ProductList.pendingTransactions, FetchUpcomingTransactionsResponse.class);

        List<UpcomingTransaction> collect = belfiusUpcomingTransaction.stream()
                .map(BelfiusUpcomingTransaction::toTinkUpcomingTransaction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        assertEquals(1, collect.size());
        assertEquals(collect.get(0).toSystemTransaction(new User()).getAmount(), -0.10, 0.0);
        assertEquals(ProductList.RandomTestData.BENEFICIARY_NAME, collect.get(0).toSystemTransaction(new User()).getDescription());
    }

}
