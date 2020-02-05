package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditAgricoleBaseTransactionalAccountFetcherTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CreditAgricoleBaseApiClient apiClient;
    private CreditAgricoleBaseTransactionalAccountFetcher transactionalAccountFetcher;
    private PersistentStorage persistentStorage;

    @Before
    public void before() {
        persistentStorage = mock(PersistentStorage.class);
        apiClient = mock(CreditAgricoleBaseApiClient.class);
        transactionalAccountFetcher =
                new CreditAgricoleBaseTransactionalAccountFetcher(apiClient, persistentStorage);
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
        List<AccountIdEntity> necessaryConsents = accountsResponse.getListOfNecessaryConsents();

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
        List<AccountIdEntity> necessaryConsents = accountsResponse.getListOfNecessaryConsents();

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
        List<AccountIdEntity> necessaryConsents = accountsResponse.getListOfNecessaryConsents();

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
    public void shouldFetchAllTransactions() {
        // given
        TransactionalAccount account = createTransactionalAccount();
        Date toDate = toDate("2020-02-01");
        Date fromDate = toDate("2019-11-01");
        when(apiClient.getTransactions(any(), any(), any()))
                .thenReturn(new GetTransactionsResponse());
        when(persistentStorage.get("isInitialFetch", Boolean.class))
                .thenReturn(Optional.of(Boolean.TRUE));

        // when
        PaginatorResponse response =
                transactionalAccountFetcher.getTransactionsFor(account, fromDate, toDate);

        // then
        verify(apiClient, times(1)).getTransactions(account.getApiIdentifier(), fromDate, toDate);
        assertFalse(response.canFetchMore().isPresent());
    }

    @Test
    public void shouldFetchLast90DaysTransactions() {
        // given
        TransactionalAccount account = createTransactionalAccount();
        Date toDate = toDate("2020-02-01");
        when(apiClient.getTransactions(any(), any(), any()))
                .thenReturn(new GetTransactionsResponse());
        when(persistentStorage.get("isInitialFetch", Boolean.class))
                .thenReturn(Optional.of(Boolean.FALSE));

        // when
        PaginatorResponse response =
                transactionalAccountFetcher.getTransactionsFor(account, null, toDate);

        // then
        verify(apiClient, times(1))
                .getTransactions(account.getApiIdentifier(), toDate("2019-11-04"), toDate);
        assertTrue(response.canFetchMore().isPresent());
        assertFalse(response.canFetchMore().get());
    }

    private GetAccountsResponse createFromJson(String json) {
        try {
            return MAPPER.readValue(json, GetAccountsResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TransactionalAccount createTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        AccountTypeMapperBuilder.build(), "CACC", TransactionalAccountType.OTHER)
                .withBalance(BalanceModule.of(ExactCurrencyAmount.of(11d, "EUR")))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("1234567890")
                                .withAccountNumber("1234567890")
                                .withAccountName("name")
                                .addIdentifier(new IbanIdentifier("1234567890"))
                                .build())
                .setApiIdentifier("bankId")
                .build()
                .get();
    }

    private Date toDate(String date) {
        LocalDate localDate = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(date));
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
