package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoStorage;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccountConsent;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.rpc.CardAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class FinecoBankCreditCardAccountFetcherTest {

    private static final String ONLY_CREDIT_CARD_ACCOUNTS_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/finecobank/resources/onlyCreditCardAccounts.json";
    private static final String TEST_CONSENT_ID = "test_consent_id";

    private FinecoBankApiClient mockApiClient;
    private FinecoStorage mockStorage;
    private FinecoBankCreditCardAccountFetcher commonTestFetcher;
    private CardAccountsResponse onlyCreditCardAccountsResponse;

    @Before
    public void setup() {
        mockApiClient = mock(FinecoBankApiClient.class);
        mockStorage = mock(FinecoStorage.class);
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);
        commonTestFetcher =
                new FinecoBankCreditCardAccountFetcher(mockApiClient, mockStorage, true);
        onlyCreditCardAccountsResponse =
                SerializationUtils.deserializeFromString(
                        new File(ONLY_CREDIT_CARD_ACCOUNTS_FILE_PATH), CardAccountsResponse.class);
    }

    private Object[] emptyCreditCardBalances() {
        return new Object[] {
            Collections.emptyList(),
            Collections.singletonList(new AccountConsent("1111", null)),
            Arrays.asList(new AccountConsent("1111", null), new AccountConsent("2222", null)),
        };
    }

    private Object[] notEmptyCreditCardBalances() {
        return new Object[] {
            Collections.singletonList(new AccountConsent(null, "1111")),
            Arrays.asList(new AccountConsent(null, "1111"), new AccountConsent(null, "2222")),
            Arrays.asList(new AccountConsent("1111", null), new AccountConsent(null, "2222")),
        };
    }

    @Test
    @Parameters(method = "emptyCreditCardBalances")
    public void shouldReturnEmptyListIfNoBalanceConsent(List<AccountConsent> balancesItems) {
        // given
        when(mockStorage.getBalancesConsents()).thenReturn(balancesItems);

        // when
        Collection<CreditCardAccount> creditCardAccounts = commonTestFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts.size()).isZero();
    }

    @Test
    @Parameters(method = "notEmptyCreditCardBalances")
    public void shouldReturnProperlyMappedCreditCards(List<AccountConsent> balancesItems) {
        // given
        when(mockStorage.getBalancesConsents()).thenReturn(balancesItems);
        when(mockApiClient.fetchCreditCardAccounts(TEST_CONSENT_ID))
                .thenReturn(onlyCreditCardAccountsResponse);

        // when
        Collection<CreditCardAccount> creditCardAccounts = commonTestFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts.size()).isEqualTo(2);

        CreditCardAccount card1 = getCardByCardNumber(creditCardAccounts, "1234 **** **** 1000");
        assertThat(card1.getCardModule().getCardNumber()).isEqualTo("1234 **** **** 1000");
        assertThat(card1.getCardModule().getBalance()).isEqualTo(ExactCurrencyAmount.of(1, "EUR"));
        assertThat(card1.getCardModule().getAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(1.123, "EUR"));
        assertThat(card1.getCardModule().getCardAlias()).isEqualTo("card_account_name_1");
        assertThat(card1.getIdModule().getUniqueId()).isEqualTo("12341000");
        assertThat(card1.getIdModule().getAccountNumber()).isEqualTo("1234 **** **** 1000");
        assertThat(card1.getIdModule().getAccountName())
                .isEqualTo("FINECO CARD VISA MULTIFUNZIONE CHIP");
        assertThat(card1.getIdModule().getIdentifiers())
                .isEqualTo(
                        Collections.singleton(
                                AccountIdentifier.create(
                                        Type.PAYMENT_CARD_NUMBER, "1234 **** **** 1000")));
        assertThat(card1.getApiIdentifier()).isEqualTo("2218836100");

        CreditCardAccount card2 = getCardByCardNumber(creditCardAccounts, "1234 **** **** 1001");
        assertThat(card2.getCardModule().getCardNumber()).isEqualTo("1234 **** **** 1001");
        assertThat(card2.getCardModule().getBalance()).isEqualTo(ExactCurrencyAmount.of(3, "EUR"));
        assertThat(card2.getCardModule().getAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(1.123, "EUR"));
        assertThat(card2.getCardModule().getCardAlias()).isEqualTo("card_account_name_2");
        assertThat(card2.getIdModule().getUniqueId()).isEqualTo("12341001");
        assertThat(card2.getIdModule().getAccountNumber()).isEqualTo("1234 **** **** 1001");
        assertThat(card2.getIdModule().getAccountName()).isEqualTo("some other unknown product");
        assertThat(card2.getIdModule().getIdentifiers())
                .isEqualTo(
                        Collections.singleton(
                                AccountIdentifier.create(
                                        Type.PAYMENT_CARD_NUMBER, "1234 **** **** 1001")));
        assertThat(card2.getApiIdentifier()).isEqualTo("2218836101");
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenThereIsNoTransactionsConsentForCardAccount() {
        // given
        CreditCardAccount account = createCreditCardAccount("accountNumber", "apiIdentifier");

        mockTransactionsConsentExistsForAccountNumbers(
                null, "", "accountNumber1", "accountNumber2");

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
                new FinecoBankCreditCardAccountFetcher(mockApiClient, mockStorage, true);

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

        when(mockApiClient.getCreditTransactions(
                        eq(TEST_CONSENT_ID), eq(account1), any(LocalDate.class)))
                .thenReturn(account1ApiResponse1)
                .thenReturn(account1ApiResponse2)
                .thenReturn(account1ApiResponse3);
        when(mockApiClient.getCreditTransactions(
                        eq(TEST_CONSENT_ID), eq(account2), any(LocalDate.class)))
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
        verify(mockApiClient)
                .getCreditTransactions(TEST_CONSENT_ID, account1, LocalDate.of(2000, 1, 1));
        verify(mockApiClient)
                .getCreditTransactions(TEST_CONSENT_ID, account1, LocalDate.of(2001, 12, 1));
        verify(mockApiClient)
                .getCreditTransactions(TEST_CONSENT_ID, account1, LocalDate.of(2002, 1, 1));

        verify(mockApiClient)
                .getCreditTransactions(TEST_CONSENT_ID, account2, LocalDate.of(2004, 1, 1));
        verify(mockApiClient)
                .getCreditTransactions(TEST_CONSENT_ID, account2, LocalDate.of(2005, 12, 1));
        verify(mockApiClient)
                .getCreditTransactions(TEST_CONSENT_ID, account2, LocalDate.of(2006, 3, 1));

        verifyNoMoreInteractions(mockApiClient);

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
                new FinecoBankCreditCardAccountFetcher(mockApiClient, mockStorage, false);

        CreditCardAccount account1 = createCreditCardAccount("accountNumber", "apiIdentifier1");
        CreditCardAccount account2 = createCreditCardAccount("accountNumber", "apiIdentifier2");
        CreditCardAccount account3 = createCreditCardAccount("accountNumber", "apiIdentifier1");

        mockTransactionsConsentExistsForAccountNumbers("accountNumber");

        PaginatorResponse account1ApiResponse1 = mock(PaginatorResponse.class);
        PaginatorResponse account2ApiResponse1 = mock(PaginatorResponse.class);

        when(mockApiClient.getCreditTransactions(
                        eq(TEST_CONSENT_ID), eq(account1), any(LocalDate.class)))
                .thenReturn(account1ApiResponse1);
        when(mockApiClient.getCreditTransactions(
                        eq(TEST_CONSENT_ID), eq(account2), any(LocalDate.class)))
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
        verify(mockApiClient)
                .getCreditTransactions(TEST_CONSENT_ID, account1, LocalDate.of(2001, 1, 1));
        verify(mockApiClient)
                .getCreditTransactions(TEST_CONSENT_ID, account2, LocalDate.of(2003, 12, 1));
        verifyNoMoreInteractions(mockApiClient);

        assertThat(account1Response1).isEqualTo(account1ApiResponse1);
        assertThat(account1Response2).isEqualTo(PaginatorResponseImpl.createEmpty(false));

        assertThat(account2Response1).isEqualTo(account2ApiResponse1);

        assertThat(account3Response2).isEqualTo(PaginatorResponseImpl.createEmpty(false));
    }

    @Test
    @Parameters(value = {"400", "429"})
    public void shouldReturnEmptyResponseOn429And400ResponseCodes(Integer responseCode) {
        // given
        CreditCardAccount account = createCreditCardAccount("irrelevant", "irrelevant");
        mockTransactionsConsentExistsForAccountNumbers("irrelevant");

        HttpResponseException responseException = httpResponseExceptionWithStatus(responseCode);
        when(mockApiClient.getCreditTransactions(any(), any(), any())).thenThrow(responseException);

        // when
        PaginatorResponse paginatorResponse =
                commonTestFetcher.getTransactionsFor(account, Year.of(1000), Month.JANUARY);

        // then
        assertThat(paginatorResponse).isEqualTo(PaginatorResponseImpl.createEmpty(false));
    }

    @Test
    @Parameters(value = {"401", "403", "404", "500"})
    public void shouldNotHandleOtherResponseCodes(int responseCode) {
        // given
        CreditCardAccount account = createCreditCardAccount("irrelevant123", "irrelevant");
        mockTransactionsConsentExistsForAccountNumbers("irrelevant123");

        HttpResponseException responseException = httpResponseExceptionWithStatus(responseCode);
        when(mockApiClient.getCreditTransactions(any(), any(), any())).thenThrow(responseException);

        // when
        Throwable t =
                catchThrowable(
                        () ->
                                commonTestFetcher.getTransactionsFor(
                                        account, Year.of(2000), Month.JANUARY));

        // then
        assertThat(t).isEqualTo(responseException);
    }

    private CreditCardAccount getCardByCardNumber(
            Collection<CreditCardAccount> cards, String accountNumber) {
        return cards.stream()
                .filter(card -> card.getCardModule().getCardNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    private void mockTransactionsConsentExistsForAccountNumbers(String... accountNumbers) {
        List<AccountConsent> transactionsConsents =
                Stream.of(accountNumbers)
                        .map(this::sampleTransactionsConsentForAccountNumber)
                        .collect(Collectors.toList());

        when(mockStorage.getTransactionsConsents()).thenReturn(transactionsConsents);
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
