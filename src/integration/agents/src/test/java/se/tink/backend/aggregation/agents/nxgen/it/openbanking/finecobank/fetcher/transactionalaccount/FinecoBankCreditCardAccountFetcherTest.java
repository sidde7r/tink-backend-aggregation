package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccountConsent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class FinecoBankCreditCardAccountFetcherTest {

    private FinecoBankApiClient finecoBankApiClient;
    private PersistentStorage persistentStorage;
    private FinecoBankCreditCardAccountFetcher commonTestFetcher;

    @Before
    public void setup() {
        finecoBankApiClient = mock(FinecoBankApiClient.class);
        persistentStorage = mock(PersistentStorage.class);
        commonTestFetcher =
                new FinecoBankCreditCardAccountFetcher(
                        finecoBankApiClient, persistentStorage, true);
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenThereIsNoTransactionsConsentForCardAccount() {
        // given
        CreditCardAccount account = createCreditCardAccount("accountNumber", "apiIdentifier");

        List<AccountConsent> transactionsConsents =
                Stream.of(
                                sampleTransactionsConsentForAccountNumber(null),
                                sampleTransactionsConsentForAccountNumber(""),
                                sampleTransactionsConsentForAccountNumber("accountNumber1"),
                                sampleTransactionsConsentForAccountNumber("accountNumber2"))
                        .collect(Collectors.toList());
        when(persistentStorage.get(
                        StorageKeys.TRANSACTIONS_CONSENTS,
                        new TypeReference<List<AccountConsent>>() {}))
                .thenReturn(Optional.of(transactionsConsents));

        // when
        Throwable t =
                catchThrowable(
                        () ->
                                commonTestFetcher.getTransactionsFor(
                                        account, Year.now(), Month.JANUARY));

        // then
        assertThat(t).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldFetchTransactionsOnly3TimesForSameAccountApiIdentifierInManualRefresh() {
        // given
        FinecoBankCreditCardAccountFetcher fetcher =
                new FinecoBankCreditCardAccountFetcher(
                        finecoBankApiClient, persistentStorage, true);

        CreditCardAccount account1 = createCreditCardAccount("accountNumber1", "apiIdentifier1");
        CreditCardAccount account2 = createCreditCardAccount("accountNumber2", "apiIdentifier2");
        CreditCardAccount account3 = createCreditCardAccount("accountNumber3", "apiIdentifier1");

        mockTransactionsConsentExistsForAccountNumbers(
                "accountNumber1", "accountNumber2", "accountNumber3");

        PaginatorResponse account1ApiResponse1 = mock(PaginatorResponse.class);
        PaginatorResponse account1ApiResponse2 = mock(PaginatorResponse.class);
        PaginatorResponse account1ApiResponse3 = mock(PaginatorResponse.class);

        PaginatorResponse account2ApiResponse1 = mock(PaginatorResponse.class);
        PaginatorResponse account2ApiResponse2 = mock(PaginatorResponse.class);
        PaginatorResponse account2ApiResponse3 = mock(PaginatorResponse.class);

        when(finecoBankApiClient.getCreditTransactions(eq(account1), any()))
                .thenReturn(account1ApiResponse1)
                .thenReturn(account1ApiResponse2)
                .thenReturn(account1ApiResponse3);
        when(finecoBankApiClient.getCreditTransactions(eq(account2), any()))
                .thenReturn(account2ApiResponse1)
                .thenReturn(account2ApiResponse2)
                .thenReturn(account2ApiResponse3);

        // when
        PaginatorResponse account1Response1 =
                fetcher.getTransactionsFor(account1, Year.of(2000), Month.JANUARY);
        PaginatorResponse account1Response2 =
                fetcher.getTransactionsFor(account1, Year.of(2001), Month.DECEMBER);
        PaginatorResponse account1Response3 =
                fetcher.getTransactionsFor(account1, Year.of(2002), Month.JANUARY);
        PaginatorResponse account1Response4 =
                fetcher.getTransactionsFor(account1, Year.of(2003), Month.JANUARY);

        PaginatorResponse account2Response1 =
                fetcher.getTransactionsFor(account2, Year.of(2004), Month.JANUARY);
        PaginatorResponse account2Response2 =
                fetcher.getTransactionsFor(account2, Year.of(2005), Month.DECEMBER);
        PaginatorResponse account2Response3 =
                fetcher.getTransactionsFor(account2, Year.of(2006), Month.MARCH);
        PaginatorResponse account2Response4 =
                fetcher.getTransactionsFor(account2, Year.of(2007), Month.JANUARY);

        PaginatorResponse account3Response1 =
                fetcher.getTransactionsFor(account3, Year.of(2020), Month.JANUARY);

        // then
        verify(finecoBankApiClient).getCreditTransactions(account1, LocalDate.of(2000, 1, 1));
        verify(finecoBankApiClient).getCreditTransactions(account1, LocalDate.of(2001, 12, 1));
        verify(finecoBankApiClient).getCreditTransactions(account1, LocalDate.of(2002, 1, 1));

        verify(finecoBankApiClient).getCreditTransactions(account2, LocalDate.of(2004, 1, 1));
        verify(finecoBankApiClient).getCreditTransactions(account2, LocalDate.of(2005, 12, 1));
        verify(finecoBankApiClient).getCreditTransactions(account2, LocalDate.of(2006, 3, 1));

        verifyNoMoreInteractions(finecoBankApiClient);

        assertThat(account1Response1).isEqualTo(account1ApiResponse1);
        assertThat(account1Response2).isEqualTo(account1ApiResponse2);
        assertThat(account1Response3).isEqualTo(account1ApiResponse3);
        assertThat(account1Response4).isEqualTo(PaginatorResponseImpl.createEmpty(false));

        assertThat(account2Response1).isEqualTo(account2ApiResponse1);
        assertThat(account2Response2).isEqualTo(account2ApiResponse2);
        assertThat(account2Response3).isEqualTo(account2ApiResponse3);
        assertThat(account2Response4).isEqualTo(PaginatorResponseImpl.createEmpty(false));

        assertThat(account3Response1).isEqualTo(PaginatorResponseImpl.createEmpty(false));
    }

    @Test
    public void shouldFetchTransactionsOnly1TimeForSameAccountApiIdentifierInAutoRefresh() {
        // given
        FinecoBankCreditCardAccountFetcher fetcher =
                new FinecoBankCreditCardAccountFetcher(
                        finecoBankApiClient, persistentStorage, false);

        CreditCardAccount account1 = createCreditCardAccount("accountNumber", "apiIdentifier1");
        CreditCardAccount account2 = createCreditCardAccount("accountNumber", "apiIdentifier2");
        CreditCardAccount account3 = createCreditCardAccount("accountNumber", "apiIdentifier1");

        mockTransactionsConsentExistsForAccountNumbers("accountNumber");

        PaginatorResponse account1ApiResponse1 = mock(PaginatorResponse.class);
        PaginatorResponse account2ApiResponse1 = mock(PaginatorResponse.class);

        when(finecoBankApiClient.getCreditTransactions(eq(account1), any()))
                .thenReturn(account1ApiResponse1);
        when(finecoBankApiClient.getCreditTransactions(eq(account2), any()))
                .thenReturn(account2ApiResponse1);

        // when
        PaginatorResponse account1Response1 =
                fetcher.getTransactionsFor(account1, Year.of(2001), Month.JANUARY);
        PaginatorResponse account1Response2 =
                fetcher.getTransactionsFor(account1, Year.of(2002), Month.FEBRUARY);

        PaginatorResponse account2Response1 =
                fetcher.getTransactionsFor(account2, Year.of(2003), Month.DECEMBER);

        PaginatorResponse account3Response2 =
                fetcher.getTransactionsFor(account3, Year.of(2004), Month.FEBRUARY);

        // then
        verify(finecoBankApiClient).getCreditTransactions(account1, LocalDate.of(2001, 1, 1));
        verify(finecoBankApiClient).getCreditTransactions(account2, LocalDate.of(2003, 12, 1));
        verifyNoMoreInteractions(finecoBankApiClient);

        assertThat(account1Response1).isEqualTo(account1ApiResponse1);
        assertThat(account1Response2).isEqualTo(PaginatorResponseImpl.createEmpty(false));

        assertThat(account2Response1).isEqualTo(account2ApiResponse1);

        assertThat(account3Response2).isEqualTo(PaginatorResponseImpl.createEmpty(false));
    }

    @Test
    public void shouldReturnEmptyResponseOn429ResponseCode() {
        // given
        CreditCardAccount account = createCreditCardAccount("irrelevant", "irrelevant");
        mockTransactionsConsentExistsForAccountNumbers("irrelevant");

        HttpResponseException responseException = httpResponseExceptionWithStatus(429);
        when(finecoBankApiClient.getCreditTransactions(any(), any())).thenThrow(responseException);

        // when
        PaginatorResponse paginatorResponse =
                commonTestFetcher.getTransactionsFor(account, Year.of(1000), Month.JANUARY);

        // then
        assertThat(paginatorResponse).isEqualTo(PaginatorResponseImpl.createEmpty(false));
    }

    @Test
    public void shouldReturnEmptyResponseOn400ResponseCode() {
        // given
        CreditCardAccount account = createCreditCardAccount("irrelevant123", "irrelevant");
        mockTransactionsConsentExistsForAccountNumbers("irrelevant123");

        HttpResponseException responseException = httpResponseExceptionWithStatus(400);
        when(finecoBankApiClient.getCreditTransactions(any(), any())).thenThrow(responseException);

        // when
        PaginatorResponse paginatorResponse =
                commonTestFetcher.getTransactionsFor(account, Year.of(2000), Month.JANUARY);

        // then
        assertThat(paginatorResponse).isEqualTo(PaginatorResponseImpl.createEmpty(false));
    }

    @Test
    @Parameters(value = {"401", "403", "404", "500"})
    public void shouldNotHandleOtherResponseCodes(String responseCodeAsString) {
        // given
        CreditCardAccount account = createCreditCardAccount("irrelevant123", "irrelevant");
        mockTransactionsConsentExistsForAccountNumbers("irrelevant123");

        HttpResponseException responseException =
                httpResponseExceptionWithStatus(Integer.parseInt(responseCodeAsString));
        when(finecoBankApiClient.getCreditTransactions(any(), any())).thenThrow(responseException);

        // when
        Throwable t =
                catchThrowable(
                        () ->
                                commonTestFetcher.getTransactionsFor(
                                        account, Year.of(2000), Month.JANUARY));

        // then
        assertThat(t).isEqualTo(responseException);
    }

    private void mockTransactionsConsentExistsForAccountNumbers(String... accountNumbers) {
        List<AccountConsent> transactionsConsents =
                Stream.of(accountNumbers)
                        .map(this::sampleTransactionsConsentForAccountNumber)
                        .collect(Collectors.toList());

        when(persistentStorage.get(eq(StorageKeys.TRANSACTIONS_CONSENTS), any(TypeReference.class)))
                .thenReturn(Optional.of(transactionsConsents));
    }

    private CreditCardAccount createCreditCardAccount(String accountNumber, String apiIdentifier) {
        CreditCardAccount account = mock(CreditCardAccount.class);
        when(account.getAccountNumber()).thenReturn(accountNumber);
        when(account.getApiIdentifier()).thenReturn(apiIdentifier);
        return account;
    }

    private AccountConsent sampleTransactionsConsentForAccountNumber(String maskedPan) {
        return new AccountConsent("irrelevant - whatever", maskedPan);
    }

    private HttpResponseException httpResponseExceptionWithStatus(int status) {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(status);

        HttpResponseException exception = mock(HttpResponseException.class);
        when(exception.getResponse()).thenReturn(response);

        return exception;
    }
}
