package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusUpcomingTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchUpcomingTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;

public class BelfiusTransactionalAccountFetcherTest extends BelfiusTest {

    @Test
    public void canFetchAccountsAndTransactions() throws Exception {
        autoAuthenticate();

        BelfiusTransactionalAccountFetcher fetcher =
                new BelfiusTransactionalAccountFetcher(this.apiClient, this.sessionStorage);

        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());

        accounts.forEach(
                account -> {
                    Collection<? extends Transaction> transactions =
                            fetcher.fetchTransactionsFor(account).getTinkTransactions();
                    assertNotNull(transactions);
                    assertFalse(transactions.isEmpty());
                });
    }

    @Test
    public void parseResponseTest() {
        FetchProductsResponse fetchProductsResponse =
                SerializationUtils.deserializeFromString(
                        ProductList.bigProductList, FetchProductsResponse.class);

        List<TransactionalAccount> accounts =
                fetchProductsResponse.stream()
                        .filter(entry -> entry.getValue().isTransactionalAccount())
                        .map(entry -> entry.getValue().toTransactionalAccount(entry.getKey()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        assertThat(accounts.get(0).getExactBalance().getDoubleValue()).isEqualTo(0);
        assertThat(accounts.get(1).getExactBalance().getDoubleValue()).isEqualTo(847.24);
        assertThat(accounts.get(2).getExactBalance().getDoubleValue()).isEqualTo(0);
        assertThat(accounts.get(3).getExactBalance().getDoubleValue()).isEqualTo(0.01);
        assertThat(accounts.get(4).getExactBalance().getDoubleValue()).isEqualTo(0);
        assertThat(accounts.get(5).getExactBalance().getDoubleValue()).isEqualTo(38374.26);
    }

    @Test
    public void parseBalanceTest() {
        FetchProductsResponse fetchProductsResponse =
                SerializationUtils.deserializeFromString(
                        ProductList.SMALL_BALANCED_PRODUCT_LIST, FetchProductsResponse.class);

        List<TransactionalAccount> accounts =
                fetchProductsResponse.stream()
                        .filter(entry -> entry.getValue().isTransactionalAccount())
                        .map(entry -> entry.getValue().toTransactionalAccount(entry.getKey()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        assertThat(accounts.get(0).getExactBalance().getDoubleValue()).isEqualTo(12);
        assertThat(accounts.get(1).getExactBalance().getDoubleValue()).isEqualTo(42);
    }

    @Test
    public void parseTransactionsTest() {
        FetchTransactionsResponse belfiusTransaction =
                SerializationUtils.deserializeFromString(
                        ProductList.transactions, FetchTransactionsResponse.class);

        List<Transaction> collect =
                belfiusTransaction.stream()
                        .map(BelfiusTransaction::toTinkTransaction)
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
        FetchUpcomingTransactionsResponse belfiusUpcomingTransaction =
                SerializationUtils.deserializeFromString(
                        ProductList.pendingTransactions, FetchUpcomingTransactionsResponse.class);

        List<UpcomingTransaction> collect =
                belfiusUpcomingTransaction.stream()
                        .map(BelfiusUpcomingTransaction::toTinkUpcomingTransaction)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        assertEquals(1, collect.size());
        assertEquals(collect.get(0).toSystemTransaction(new User()).getAmount(), -0.10, 0.0);
        assertEquals(
                ProductList.RandomTestData.BENEFICIARY_NAME,
                collect.get(0).toSystemTransaction(new User()).getDescription());
    }
}
