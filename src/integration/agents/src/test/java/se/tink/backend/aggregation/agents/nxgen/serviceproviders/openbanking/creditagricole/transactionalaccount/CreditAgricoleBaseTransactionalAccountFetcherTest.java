package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleTestFixtures.ACCOUNT_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CreditAgricoleBaseTransactionalAccountFetcherTest {

    private static final ZoneId ZONE_ID = ZoneId.of("CET");
    private static final Instant NOW = Instant.now();
    private static final LocalDate TODAY = NOW.atZone(ZONE_ID).toLocalDate();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CreditAgricoleBaseApiClient apiClient;
    private CreditAgricoleBaseTransactionalAccountFetcher transactionalAccountFetcher;
    private PersistentStorage persistentStorage;

    @Before
    public void before() {
        persistentStorage = mock(PersistentStorage.class);
        apiClient = mock(CreditAgricoleBaseApiClient.class);

        final Clock clockMock = createClockMock();
        transactionalAccountFetcher =
                new CreditAgricoleBaseTransactionalAccountFetcher(
                        apiClient, persistentStorage, clockMock);
    }

    @Test
    public void shouldFetchAccountsWithNoNecessaryConsents() {
        // given
        GetAccountsResponse accountsResponse =
                createFromJson(
                        CreditAgricoleBaseTransactionalAccountFetcherTestData
                                .ACCOUNT_WITH_ALL_LINKS);
        Collection<TransactionalAccount> transactionalAccounts = accountsResponse.toTinkAccounts();
        when(apiClient.getAccounts()).thenReturn(accountsResponse);

        // when
        Collection<TransactionalAccount> resp = transactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertEquals(transactionalAccounts, resp);
        verify(apiClient, times(1)).getAccounts();
        verify(apiClient, never()).putConsents(any());
    }

    @Test
    public void shouldFetchAccountsWithNecessaryConsentsWhenTransactionsLinkMissing() {
        // given
        GetAccountsResponse accountsResponse =
                createFromJson(
                        CreditAgricoleBaseTransactionalAccountFetcherTestData
                                .ACCOUNT_WITHOUT_TRANSACTIONS_LINK);
        Collection<TransactionalAccount> transactionalAccounts = accountsResponse.toTinkAccounts();
        List<AccountIdEntity> necessaryConsents =
                accountsResponse.getAccountsListForNecessaryConsents();

        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        doNothing().when(apiClient).putConsents(necessaryConsents);

        // when
        Collection<TransactionalAccount> resp = transactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertEquals(transactionalAccounts, resp);
        verify(apiClient, times(2)).getAccounts();
        verify(apiClient, times(1)).putConsents(necessaryConsents);
    }

    @Test
    public void shouldFetchAccountsWithNecessaryConsentsWhenIdentityLinkMissing() {
        // given
        GetAccountsResponse accountsResponse =
                createFromJson(
                        CreditAgricoleBaseTransactionalAccountFetcherTestData
                                .ACCOUNTS_WITHOUT_IDENTITY_LINK);
        Collection<TransactionalAccount> transactionalAccounts = accountsResponse.toTinkAccounts();
        List<AccountIdEntity> necessaryConsents =
                accountsResponse.getAccountsListForNecessaryConsents();

        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        doNothing().when(apiClient).putConsents(necessaryConsents);

        // when
        Collection<TransactionalAccount> resp = transactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertEquals(transactionalAccounts, resp);
        verify(apiClient, times(2)).getAccounts();
        verify(apiClient, times(1)).putConsents(necessaryConsents);
    }

    @Test
    public void shouldFetchAccountsWithNecessaryConsentsWhenLinksMissing() {
        // given
        GetAccountsResponse accountsResponse =
                createFromJson(
                        CreditAgricoleBaseTransactionalAccountFetcherTestData
                                .ACCOUNTS_WITHOUT_LINKS);
        Collection<TransactionalAccount> transactionalAccounts = accountsResponse.toTinkAccounts();
        List<AccountIdEntity> necessaryConsents =
                accountsResponse.getAccountsListForNecessaryConsents();

        when(apiClient.getAccounts()).thenReturn(accountsResponse);
        doNothing().when(apiClient).putConsents(necessaryConsents);

        // when
        Collection<TransactionalAccount> resp = transactionalAccountFetcher.fetchAccounts();

        // then
        assertNotNull(resp);
        assertEquals(transactionalAccounts, resp);
        verify(apiClient, times(2)).getAccounts();
        verify(apiClient, times(1)).putConsents(necessaryConsents);
    }

    @Test
    public void shouldGetTransactionsWithin13MonthsLimit() {
        // given
        final TransactionalAccount account = createAccountMock();
        final LocalDate todayMinus13Months = TODAY.minusMonths(13L);
        final LocalDate todayMinus10Months = TODAY.minusMonths(10L);
        final GetTransactionsResponse expectedResponse = createGetTransactionsResponse();

        when(apiClient.getTransactions(ACCOUNT_ID, todayMinus13Months, todayMinus10Months))
                .thenReturn(expectedResponse);

        when(persistentStorage.get("isInitialFetch", Boolean.class))
                .thenReturn(Optional.of(Boolean.TRUE));

        // when
        final PaginatorResponse returnedResponse =
                transactionalAccountFetcher.getTransactionsFor(
                        account,
                        convertLocalDateToDate(todayMinus13Months),
                        convertLocalDateToDate(todayMinus10Months));

        // then
        assertThat(returnedResponse.getTinkTransactions()).isNotEmpty();
        assertThat(returnedResponse.canFetchMore().isPresent()).isFalse();
    }

    @Test
    public void shouldGetTransactionsWhenSomeAreBeyond13MonthsLimit() {
        // given
        final TransactionalAccount account = createAccountMock();
        final LocalDate todayMinus14Months = TODAY.minusMonths(14L);
        final LocalDate todayMinus13Months = TODAY.minusMonths(13L);
        final GetTransactionsResponse expectedResponse = createGetTransactionsResponse();

        when(apiClient.getTransactions(ACCOUNT_ID, todayMinus13Months, todayMinus13Months))
                .thenReturn(expectedResponse);

        when(persistentStorage.get("isInitialFetch", Boolean.class))
                .thenReturn(Optional.of(Boolean.TRUE));

        // when
        final PaginatorResponse returnedResponse =
                transactionalAccountFetcher.getTransactionsFor(
                        account,
                        convertLocalDateToDate(todayMinus14Months),
                        convertLocalDateToDate(todayMinus13Months));

        // then
        assertThat(returnedResponse.getTinkTransactions()).isNotEmpty();
        assertThat(returnedResponse.canFetchMore().isPresent()).isFalse();
    }

    @Test
    public void shouldGetNoTransactionsWhenAllAreBeyond13MonthsLimit() {
        // given
        final TransactionalAccount account = createAccountMock();
        final LocalDate todayMinus15Months = TODAY.minusMonths(15L);
        final LocalDate todayMinus14Months = TODAY.minusMonths(14L);

        when(persistentStorage.get("isInitialFetch", Boolean.class))
                .thenReturn(Optional.of(Boolean.TRUE));

        // when
        final PaginatorResponse returnedResponse =
                transactionalAccountFetcher.getTransactionsFor(
                        account,
                        convertLocalDateToDate(todayMinus15Months),
                        convertLocalDateToDate(todayMinus14Months));

        // then
        assertThat(returnedResponse.getTinkTransactions()).isEmpty();
        assertThat(returnedResponse.canFetchMore().isPresent()).isTrue();
        assertThat(returnedResponse.canFetchMore().get()).isFalse();

        verify(apiClient, never()).getTransactions(anyString(), any(), any());
    }

    @Test
    public void shouldFetchLast90DaysTransactions() {
        // given
        final TransactionalAccount account = createAccountMock();
        final LocalDate todayMinus89Days = TODAY.minusDays(89);
        final GetTransactionsResponse expectedResponse = createGetTransactionsResponse();

        when(apiClient.getTransactions(ACCOUNT_ID, todayMinus89Days, TODAY))
                .thenReturn(expectedResponse);
        when(persistentStorage.get("isInitialFetch", Boolean.class))
                .thenReturn(Optional.of(Boolean.FALSE));

        // when
        PaginatorResponse response =
                transactionalAccountFetcher.getTransactionsFor(
                        account, null, convertLocalDateToDate(TODAY));

        // then
        assertThat(response.canFetchMore().isPresent()).isTrue();
        assertThat(response.canFetchMore().get()).isFalse();
        assertThat(response.getTinkTransactions()).isNotEmpty();
    }

    private GetAccountsResponse createFromJson(String json) {
        try {
            return MAPPER.readValue(json, GetAccountsResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GetTransactionsResponse createGetTransactionsResponse() {
        final TransactionEntity transactionEntityMock = mock(TransactionEntity.class);
        when(transactionEntityMock.toTinkTransaction()).thenReturn(mock(Transaction.class));

        final GetTransactionsResponse getTransactionsResponse = new GetTransactionsResponse();

        getTransactionsResponse.setTransactions(Collections.singletonList(transactionEntityMock));

        return getTransactionsResponse;
    }

    private static Clock createClockMock() {
        final Clock clockMock = mock(Clock.class);

        when(clockMock.instant()).thenReturn(NOW);
        when(clockMock.getZone()).thenReturn(ZONE_ID);

        return clockMock;
    }

    private static TransactionalAccount createAccountMock() {
        final TransactionalAccount accountMock = mock(TransactionalAccount.class);

        when(accountMock.getApiIdentifier()).thenReturn(ACCOUNT_ID);

        return accountMock;
    }

    private static Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZONE_ID).toInstant());
    }
}
