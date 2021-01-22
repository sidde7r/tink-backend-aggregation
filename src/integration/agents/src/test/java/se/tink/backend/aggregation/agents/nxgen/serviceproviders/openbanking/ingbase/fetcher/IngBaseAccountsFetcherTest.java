package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IngBaseAccountsFetcherTest {
    private static final String TEST_CURRENCY = "EUR";

    private IngBaseApiClient apiClient = mock(IngBaseApiClient.class);
    private static final FetchAccountsResponse ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"accounts\" : [{\"_links\" : { \"transactions\" : {\"href\" : \"HREF\"}} , \"iban\" : \"PL666\", \"resourceId\" : \"1\", \"name\" : \"NAME\", \"currency\" : \"EUR\", \"product\" : \"PRODUCT\"}]}",
                    FetchAccountsResponse.class);

    private static final FetchBalancesResponse BALANCE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\"balances\" : [ { \"balanceType\" : \"closingBooked\", \"balanceAmount\" : {\"currency\" : \"EUR\", \"amount\" : 12.12 }}, { \"balanceType\" : \"expected\", \"balanceAmount\" : {\"currency\" : \"EUR\", \"amount\" : 12.12 }} ] }",
                    FetchBalancesResponse.class);

    private IngBaseAccountsFetcher fetcher =
            new IngBaseAccountsFetcher(apiClient, TEST_CURRENCY, false);

    @Test
    public void shouldFetchAndMapCheckingTransactionalAccount() {
        // given
        AccountEntity accountEntity =
                ACCOUNTS_RESPONSE.getTransactionalAccounts(TEST_CURRENCY).iterator().next();
        given(apiClient.fetchAccounts()).willReturn(ACCOUNTS_RESPONSE);
        given(apiClient.fetchBalances(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        TransactionalAccount account = accounts.iterator().next();
        assertThat(account.getAccountNumber()).isEqualTo("PL666");
        assertThat(account.getName()).isEqualTo("PRODUCT");
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(12.12));
    }

    @Test
    public void shouldFetchAndMapSavingsTransactionalAccount() {
        // given
        AccountEntity accountEntity =
                ACCOUNTS_RESPONSE.getTransactionalAccounts(TEST_CURRENCY).iterator().next();
        given(apiClient.fetchAccounts()).willReturn(ACCOUNTS_RESPONSE);
        given(apiClient.fetchBalances(accountEntity)).willReturn(BALANCE_RESPONSE);

        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        TransactionalAccount account = accounts.iterator().next();
        assertThat(account.getAccountNumber()).isEqualTo("PL666");
        assertThat(account.getName()).isEqualTo("PRODUCT");
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.inEUR(12.12));
    }
}
