package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BelfiusTransactionalAccountFetcherTest extends BelfiusTest {

    @Test
    public void canFetchAccountsAndTransactions() throws Exception {
        autoAuthenticate();

        BelfiusTransactionalAccountFetcher fetcher = new BelfiusTransactionalAccountFetcher(this.apiClient);

        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());

        Date toDate = new Date();
        Date fromDate = DateUtils.addMonths(toDate, -1);
        accounts.forEach(account -> {
            Collection<Transaction> transactions = fetcher.getTransactionsFor(account, fromDate, toDate);
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
        assertTrue(collect.get(0).toSystemTransaction().getAmount() == -5510.08);
        assertTrue(collect.get(1).toSystemTransaction().getAmount() == -5123510.08);
        assertTrue(collect.get(2).toSystemTransaction().getAmount() == 5510.08);
        assertTrue(collect.get(3).toSystemTransaction().getAmount() == 5123510.08);
        assertTrue(collect.get(4).toSystemTransaction().getAmount() == -103.6);
        assertTrue(collect.get(5).toSystemTransaction().getAmount() == 103.6);
        assertTrue(collect.get(6).toSystemTransaction().getAmount() == 103.6012);
        assertTrue(collect.get(7).toSystemTransaction().getAmount() == -0.35);
    }

}
