package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusUpcomingTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchUpcomingTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BelfiusTransactionalAccountFetcherTest {

    private BelfiusApiClient apiClient;

    private BelfiusTransactionalAccountFetcher fetcher;

    @Before
    public void setUp() {
        apiClient = mock(BelfiusApiClient.class);
        BelfiusSessionStorage sessionStorage = mock(BelfiusSessionStorage.class);

        fetcher = new BelfiusTransactionalAccountFetcher(apiClient, sessionStorage);
    }

    @Test
    public void shouldReturnAccounts() {
        // given
        FetchProductsResponse fpr =
                SerializationUtils.deserializeFromString(
                        ProductList.oneProductList, FetchProductsResponse.class);
        // and
        given(apiClient.fetchProducts()).willReturn(fpr);

        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        TransactionalAccount[] transactionalAccounts =
                accounts.toArray(new TransactionalAccount[0]);

        assertThat(transactionalAccounts[0].getName()).isEqualTo("BELFIasdas");
        assertThat(transactionalAccounts[0].getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(transactionalAccounts[0].getExactBalance())
                .isEqualTo(ExactCurrencyAmount.inEUR(123.45));

        assertThat(accounts.size()).isEqualTo(1);
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

        assertThat(accounts.get(0).getExactBalance().getDoubleValue()).isZero();
        assertThat(accounts.get(1).getExactBalance().getDoubleValue()).isEqualTo(847.24);
        assertThat(accounts.get(2).getExactBalance().getDoubleValue()).isZero();
        assertThat(accounts.get(3).getExactBalance().getDoubleValue()).isEqualTo(0.01);
        assertThat(accounts.get(4).getExactBalance().getDoubleValue()).isZero();
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

        assertThat(collect.size()).isEqualTo(8);
        assertThat(collect.get(0).toSystemTransaction(false).getAmount()).isEqualTo(-5510.08);
        assertThat(collect.get(1).toSystemTransaction(false).getAmount()).isEqualTo(-5123510.08);
        assertThat(collect.get(2).toSystemTransaction(false).getAmount()).isEqualTo(5510.08);
        assertThat(collect.get(3).toSystemTransaction(false).getAmount()).isEqualTo(5123510.08);
        assertThat(collect.get(4).toSystemTransaction(false).getAmount()).isEqualTo(-103.6);
        assertThat(collect.get(5).toSystemTransaction(false).getAmount()).isEqualTo(103.6);
        assertThat(collect.get(6).toSystemTransaction(false).getAmount()).isEqualTo(103.6012);
        assertThat(collect.get(7).toSystemTransaction(false).getAmount()).isEqualTo(-0.35);
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

        assertThat(collect.size()).isEqualTo(1);
        assertThat(collect.get(0).toSystemTransaction(false).getAmount()).isEqualTo(-0.10);
        assertThat(collect.get(0).toSystemTransaction(false).getDescription())
                .isEqualTo(ProductList.RandomTestData.BENEFICIARY_NAME);
    }
}
