package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role.HOLDER;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.CertainDateTransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RedsysTransactionalAccountFetcherTest {

    private static final String ACCOUNT_RESPONSE =
            "{\"accounts\":[{\"resourceId\":\"ES018202000000000000000000000222222\",\"iban\":\"ES1714658336187317761933\",\"currency\":\"EUR\",\"ownerName\":\"JOHN DOE\",\"status\":\"enabled\",\"product\":\"CUENTA ONLINE\",\"balances\":[{\"balanceAmount\":{\"currency\":\"EUR\",\"amount\":\"1543.71\"},\"balanceType\":\"closingBooked\"},{\"balanceAmount\":{\"currency\":\"EUR\",\"amount\":\"1543.71\"},\"balanceType\":\"interimAvailable\"}],\"_links\":{\"balances\":{\"href\":\"/v1/accounts/ES018202000000000000000000000222222/balances\"},\"transactions\":{\"href\":\"/v1/accounts/ES018202000000000000000000000222222/transactions?withBalance=true&bookingStatus=both\"}}},{\"resourceId\":\"ES018202000000000000000000000222222\",\"iban\":\"ES6120957422817788187146\",\"currency\":\"EUR\",\"ownerName\":\"JOHN DOE\",\"cashAccountType\":\"SVGS\",\"status\":\"enabled\",\"product\":\"CUENTA VA CONTIGO\",\"balances\":[{\"balanceAmount\":{\"currency\":\"EUR\",\"amount\":\"1440.30\"},\"balanceType\":\"closingBooked\"},{\"balanceAmount\":{\"currency\":\"EUR\",\"amount\":\"1440.30\"},\"balanceType\":\"interimAvailable\"}],\"_links\":{\"balances\":{\"href\":\"/v1/accounts/ES018202000000000000000000000222222/balances\"},\"transactions\":{\"href\":\"/v1/accounts/ES018202000000000000000000000222222/transactions?withBalance=true&bookingStatus=both\"}}}]}";
    private RedsysTransactionalAccountFetcher accountFetcher;
    private RedsysApiClient apiClient;
    private RedsysConsentController consentController;
    private TransactionPaginationHelper paginationHelper;
    private AspspConfiguration aspspConfiguration;

    @Before
    public void setup() {
        apiClient = mock(RedsysApiClient.class);
        consentController = mock(RedsysConsentController.class);
        when(consentController.getConsentId()).thenReturn("dummyConsentId");
        paginationHelper = mock(CertainDateTransactionPaginationHelper.class);
        aspspConfiguration = mock(AspspConfiguration.class);
        accountFetcher =
                new RedsysTransactionalAccountFetcher(
                        apiClient, consentController, aspspConfiguration, paginationHelper);
    }

    @Test
    public void shouldFetchAndMapAccounts() {
        // given
        when(apiClient.fetchAccounts(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                ACCOUNT_RESPONSE, ListAccountsResponse.class));
        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();
        accounts.iterator();

        // then
        Iterator<TransactionalAccount> iterator = accounts.iterator();
        assertFirstAccountValid(iterator.next());
        assertSecondAccountValid(iterator.next());
    }

    private void assertFirstAccountValid(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("1543.71"));
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getAccountFlags()).contains(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        assertThat(account.getIdModule().getAccountName()).isEqualTo("CUENTA ONLINE");
        assertThat(account.getIdModule().getAccountNumber())
                .isEqualTo("ES17 1465 8336 1873 1776 1933");
        assertThat(account.getParties().get(0).getName()).isEqualTo("John Doe");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(HOLDER);
    }

    private void assertSecondAccountValid(TransactionalAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("1440.30"));
        assertThat(account.getType()).isEqualTo(AccountTypes.SAVINGS);
        assertThat(account.getAccountFlags()).contains(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        assertThat(account.getIdModule().getAccountName()).isEqualTo("CUENTA VA CONTIGO");
        assertThat(account.getIdModule().getAccountNumber())
                .isEqualTo("ES61 2095 7422 8177 8818 7146");
        assertThat(account.getParties().get(0).getName()).isEqualTo("John Doe");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(HOLDER);
    }

    @Test
    public void fetch_transactions_should_pagiante_if_nextKey_available() {
        // given

        TransactionalAccount account = mock(TransactionalAccount.class);
        when(paginationHelper.getTransactionDateLimit(account))
                .thenReturn(Optional.of(new Date(1638892290)));

        BaseTransactionsResponse transactionsWithKey = mock(BaseTransactionsResponse.class);
        PaginationKey paginationKey = mock(PaginationKey.class);
        when(transactionsWithKey.nextKey()).thenReturn(paginationKey);
        BaseTransactionsResponse transactionsWithoutKey = mock(BaseTransactionsResponse.class);
        when(transactionsWithoutKey.nextKey()).thenReturn(null);

        when(apiClient.fetchTransactions(any(), any(), eq(LocalDate.of(1970, 1, 19))))
                .thenReturn(transactionsWithKey);

        when(apiClient.fetchTransactionsWithKey(any(), any())).thenReturn(transactionsWithoutKey);

        // when
        accountFetcher.fetchTransactionsFor(account);

        // then
        verify(apiClient).fetchTransactions(any(), any(), eq(LocalDate.of(1970, 1, 19)));
        verify(apiClient).fetchTransactionsWithKey(any(), any());
    }

    @Test
    public void fetch_transactions_should_not_pagiante_if_nextKey_available() {
        TransactionalAccount account = mock(TransactionalAccount.class);
        BaseTransactionsResponse transactionsWithKey = mock(BaseTransactionsResponse.class);

        when(paginationHelper.getTransactionDateLimit(account))
                .thenReturn(Optional.of(new Date(1638892290)));

        when(transactionsWithKey.nextKey()).thenReturn(null);

        when(apiClient.fetchTransactions(any(), any(), eq(LocalDate.of(1970, 1, 19))))
                .thenReturn(transactionsWithKey);

        // when
        accountFetcher.fetchTransactionsFor(account);

        // then
        verify(apiClient).fetchTransactions(any(), any(), eq(LocalDate.of(1970, 1, 19)));
        verify(apiClient, never()).fetchTransactionsWithKey(any(), any());
    }

    @Test
    public void fetch_transactions_should_ask_for_upcoming_transactions_if_supported() {
        TransactionalAccount account = mock(TransactionalAccount.class);

        when(paginationHelper.getTransactionDateLimit(account))
                .thenReturn(Optional.of(new Date(1638892290)));
        BaseTransactionsResponse transactions = mock(BaseTransactionsResponse.class);
        when(transactions.nextKey()).thenReturn(null);

        when(apiClient.fetchTransactions(any(), any(), eq(LocalDate.of(1970, 1, 19))))
                .thenReturn(transactions);
        when(apiClient.fetchPendingTransactions(any(), any())).thenReturn(transactions);

        when(aspspConfiguration.supportsPendingTransactions()).thenReturn(true);

        // when
        accountFetcher.fetchTransactionsFor(account);

        // then
        verify(apiClient).fetchTransactions(any(), any(), eq(LocalDate.of(1970, 1, 19)));
        verify(apiClient).fetchPendingTransactions(any(), any());
    }
}
