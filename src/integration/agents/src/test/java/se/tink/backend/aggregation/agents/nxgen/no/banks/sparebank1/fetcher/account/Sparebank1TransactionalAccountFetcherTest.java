package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.TestHelper.mockHttpClient;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.exceptions.refresh.CheckingAccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.rpc.AccountApiIdentifiersResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Sparebank1TransactionalAccountFetcherTest {
    private RequestBuilder requestBuilder;
    private Sparebank1ApiClient apiClient;
    private Sparebank1TransactionalAccountFetcher fetcher;

    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebank1/resources";

    @Before
    public void init() {
        requestBuilder = mock(RequestBuilder.class);
        HttpResponse response = mock(HttpResponse.class);
        TinkHttpClient client = mockHttpClient(requestBuilder, response);
        apiClient = new Sparebank1ApiClient(client, "dummyBankId");
        fetcher = new Sparebank1TransactionalAccountFetcher(apiClient);
    }

    @Test
    public void fetchAccountsShouldReturnEmptyListIfNoAccountsReceived() {
        // given
        when(requestBuilder.get(AccountListResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "account_response_empty.json").toFile(),
                                AccountListResponse.class));
        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        assertThat(accounts.isEmpty()).isTrue();
    }

    @Test
    public void fetchAccountsShouldReturnTinkAccounts() {
        // given
        when(requestBuilder.get(AccountListResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "account_response.json").toFile(),
                                AccountListResponse.class));
        when(requestBuilder.get(AccountApiIdentifiersResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "api_identifiers_response.json").toFile(),
                                AccountApiIdentifiersResponse.class));
        when(requestBuilder.get(AccountDetailsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "account_details_response.json").toFile(),
                                AccountDetailsResponse.class));

        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(2);
        assertSavingAccount(accounts);
        assertCheckingAccount(accounts);
    }

    private void assertCheckingAccount(Collection<TransactionalAccount> accounts) {
        TransactionalAccount checkingAccount =
                accounts.stream()
                        .filter(acc -> acc.getType().equals(AccountTypes.CHECKING))
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);
        assertThat(checkingAccount.getAccountNumber()).isEqualTo("accountNumber1");
        assertThat(checkingAccount.getApiIdentifier()).isEqualTo("key1");
        assertThat(checkingAccount.getName()).isEqualTo("account1");
        assertThat(checkingAccount.getHolderName()).isEqualTo(new HolderName("ownerDummyName"));
        assertThat(checkingAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(3.51), "NOK"));
        assertThat(checkingAccount.getExactAvailableBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(-2.32), "NOK"));
        assertThat(checkingAccount.getExactCreditLimit())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(2.340), "NOK"));
    }

    private void assertSavingAccount(Collection<TransactionalAccount> accounts) {
        TransactionalAccount savingAccount =
                accounts.stream()
                        .filter(acc -> acc.getType().equals(AccountTypes.SAVINGS))
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);
        assertThat(savingAccount.getAccountNumber()).isEqualTo("accountNumber2");
        assertThat(savingAccount.getApiIdentifier()).isEqualTo("key2");
        assertThat(savingAccount.getName()).isEqualTo("account2");
        assertThat(savingAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(0.00), "NOK"));
        assertThat(savingAccount.getExactAvailableBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(0.00), "NOK"));
    }

    @Test
    public void fetchAccountsShouldThrowExceptionIfApiIdentifierNotMapped() {
        // given
        when(requestBuilder.get(AccountListResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "account_response.json").toFile(),
                                AccountListResponse.class));
        when(requestBuilder.get(AccountApiIdentifiersResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(
                                                RESOURCE_PATH,
                                                "api_identifiers_response_not_matching.json")
                                        .toFile(),
                                AccountApiIdentifiersResponse.class));

        // when
        Throwable throwable = catchThrowable(() -> fetcher.fetchAccounts());

        // then
        assertThat(throwable).isInstanceOf(CheckingAccountRefreshException.class);
    }
}
