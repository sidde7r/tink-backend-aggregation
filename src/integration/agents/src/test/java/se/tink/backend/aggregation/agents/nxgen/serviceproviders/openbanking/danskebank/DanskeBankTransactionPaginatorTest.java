package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.identifiers.OtherIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.unleash.UnleashClient;

@RunWith(MockitoJUnitRunner.class)
public class DanskeBankTransactionPaginatorTest {

    @Mock private AgentComponentProvider componentProvider;
    @Mock private UnleashClient unleashClient;
    @Mock private CompositeAgentContext context;
    @Mock private Provider provider;

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/danskebank/resources";
    private static final String NEXT_PAGE_OF_TRANSACTIONS =
            "https://psd2-api.danskebank.com/psd2/v3.1/aisp/accounts/BI123/transactions?fromBookingDateTime=2019-07-23T00:00:00&toBookingDateTime=2021-06-23T00:00:00&pg=df5dde55-234b-4732-9a32-b623e0e1bb11&int=b95e85fc-3706-4e9c-abb2-d911dabb27fb";
    private static final String LAST_23_MONTHS_TRANSACTIONS_PATH =
            "/some/path?fromBookingDateTime=2018-07-02T00:00:00Z";
    private static final String LAST_90_DAYS_PATH =
            "/some/path?fromBookingDateTime=2020-03-05T00:00:00Z";

    private DanskeBankTransactionPaginator<AccountTransactionsV31Response, TransactionalAccount>
            paginator;
    private PersistentStorage persistentStorage;

    private UkOpenBankingApiClient apiClient;

    private final ArgumentCaptor<String> persistentStorageCaptor =
            ArgumentCaptor.forClass(String.class);

    @Before
    public void init() {
        UkOpenBankingAisConfig ukOpenBankingAisConfig = mock(UkOpenBankingAisConfig.class);
        when(ukOpenBankingAisConfig.getInitialTransactionsPaginationKey("BI123"))
                .thenReturn("/some/path");
        when(componentProvider.getContext()).thenReturn(context);
        when(context.getAppId()).thenReturn("mockedAppId");
        when(componentProvider.getUnleashClient()).thenReturn(unleashClient);
        when(provider.getName()).thenReturn("providerName");

        DelaySimulatingLocalDateTimeSource localDateTimeSource =
                new DelaySimulatingLocalDateTimeSource(LocalDateTime.parse("2020-06-02T00:00:00"));

        apiClient = mock(UkOpenBankingApiClient.class);
        when(apiClient.fetchAccountTransactions(any(), eq(AccountTransactionsV31Response.class)))
                .then(
                        (Answer<AccountTransactionsV31Response>)
                                invocation -> {
                                    localDateTimeSource
                                            .increment(); // simulate a delay if api is called
                                    return new AccountTransactionsV31Response();
                                });

        persistentStorage = mock(PersistentStorage.class);

        paginator =
                new DanskeBankTransactionPaginator<>(
                        componentProvider,
                        provider,
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        (response, account) ->
                                AccountTransactionsV31Response
                                        .toAccountTransactionPaginationResponse(response),
                        localDateTimeSource);
    }

    @Test
    public void testRequestTimeProperlySaved() {
        TransactionalAccount account = getTestAccount();

        paginator.getTransactionsFor(account, null);

        verify(persistentStorage)
                .put(eq("fetchedTxUntil:BI123"), persistentStorageCaptor.capture());

        verify(apiClient)
                .fetchAccountTransactions(
                        eq(LAST_23_MONTHS_TRANSACTIONS_PATH),
                        eq(AccountTransactionsV31Response.class));

        assertEquals("2020-06-02T00:00:00", persistentStorageCaptor.getValue());
    }

    @Test
    public void testFetchTransactionsForShortPeriod() {
        when(persistentStorage.get("fetchedTxUntil:BI123")).thenReturn("2020-06-02T00:00:00");

        TransactionalAccount account = getTestAccount();

        paginator.getTransactionsFor(account, null);

        verify(apiClient)
                .fetchAccountTransactions(
                        eq(LAST_90_DAYS_PATH), eq(AccountTransactionsV31Response.class));
    }

    @Test
    public void shouldFetchOnePageOfTransactionsWhenThereIsNoKey() {
        // given
        when(apiClient.fetchAccountTransactions(
                        eq(LAST_23_MONTHS_TRANSACTIONS_PATH),
                        eq(AccountTransactionsV31Response.class)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "transactions_one_page.json").toFile(),
                                AccountTransactionsV31Response.class));

        // when
        TransactionKeyPaginatorResponse<String> result =
                paginator.getTransactionsFor(getTestAccount(), null);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        new TransactionKeyPaginatorResponseImpl<String>(
                                getExpectedLastPageTransactions(), null));
    }

    @Test
    public void shouldFetchOnePageOfTransactionsFromLast90DaysWithRetryAfterException() {
        // given
        HttpResponseException httpResponseException = mockErrorResponse();
        AccountTransactionsV31Response response =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactions_one_page.json").toFile(),
                        AccountTransactionsV31Response.class);
        when(apiClient.fetchAccountTransactions(
                        eq(LAST_23_MONTHS_TRANSACTIONS_PATH),
                        eq(AccountTransactionsV31Response.class)))
                .thenThrow(httpResponseException);

        when(apiClient.fetchAccountTransactions(
                        eq(LAST_90_DAYS_PATH), eq(AccountTransactionsV31Response.class)))
                .thenReturn(response);

        // when
        TransactionKeyPaginatorResponse<String> result =
                paginator.getTransactionsFor(getTestAccount(), null);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        new TransactionKeyPaginatorResponseImpl<String>(
                                getExpectedLastPageTransactions(), null));
    }

    @Test
    public void shouldFetchMultiplePagesOfTransactions() {
        // given
        when(apiClient.fetchAccountTransactions(
                        eq(LAST_23_MONTHS_TRANSACTIONS_PATH),
                        eq(AccountTransactionsV31Response.class)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "transactions_with_next_key.json")
                                        .toFile(),
                                AccountTransactionsV31Response.class));

        when(apiClient.fetchAccountTransactions(
                        eq(NEXT_PAGE_OF_TRANSACTIONS), eq(AccountTransactionsV31Response.class)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "transactions_one_page.json").toFile(),
                                AccountTransactionsV31Response.class));

        // when
        TransactionKeyPaginatorResponse<String> result =
                paginator.getTransactionsFor(getTestAccount(), null);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        new TransactionKeyPaginatorResponseImpl<String>(
                                ListUtils.union(
                                        (List<Transaction>) getExpectedFirstPageTransactions(),
                                        (List<Transaction>) getExpectedLastPageTransactions()),
                                null));
    }

    @Test
    public void shouldFetchMultiplePagesOfTransactionsWithRetryAfterException() {
        // given
        HttpResponseException httpResponseException = mockErrorResponse();
        when(apiClient.fetchAccountTransactions(
                        eq(LAST_23_MONTHS_TRANSACTIONS_PATH),
                        eq(AccountTransactionsV31Response.class)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "transactions_with_next_key.json")
                                        .toFile(),
                                AccountTransactionsV31Response.class));

        when(apiClient.fetchAccountTransactions(
                        eq(NEXT_PAGE_OF_TRANSACTIONS), eq(AccountTransactionsV31Response.class)))
                .thenThrow(httpResponseException)
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "transactions_one_page.json").toFile(),
                                AccountTransactionsV31Response.class));

        // when
        TransactionKeyPaginatorResponse<String> result =
                paginator.getTransactionsFor(getTestAccount(), null);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        new TransactionKeyPaginatorResponseImpl<String>(
                                ListUtils.union(
                                        (List<Transaction>) getExpectedFirstPageTransactions(),
                                        (List<Transaction>) getExpectedLastPageTransactions()),
                                null));
    }

    @Test
    public void shouldThrowExceptionAfterRetry() {
        // given
        HttpResponseException httpResponseException = mockErrorResponse();
        when(apiClient.fetchAccountTransactions(any(), eq(AccountTransactionsV31Response.class)))
                .thenThrow(httpResponseException);

        // when
        Throwable thrown =
                catchThrowable(() -> paginator.getTransactionsFor(getTestAccount(), null));

        // then
        assertThat(thrown).isInstanceOf(HttpResponseException.class);
    }

    private Collection<Transaction> getExpectedFirstPageTransactions() {
        return Arrays.asList(
                (Transaction)
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.inDKK(11.12))
                                .setDescription("Some transaction")
                                .setPending(false)
                                .setMutable(true)
                                .setDate(Date.from(Instant.parse("2019-10-29T00:00:00Z")))
                                .setTransactionDates(
                                        TransactionDates.builder()
                                                .setBookingDate(
                                                        new AvailableDateInformation()
                                                                .setInstant(null)
                                                                .setDate(
                                                                        LocalDate.parse(
                                                                                "2019-10-29")))
                                                .setValueDate(
                                                        new AvailableDateInformation()
                                                                .setInstant(null)
                                                                .setDate(
                                                                        LocalDate.parse(
                                                                                "2019-10-29")))
                                                .build())
                                .setTransactionReference("REF1")
                                .setProviderMarket("UK")
                                .addExternalSystemIds(
                                        TransactionExternalSystemIdType
                                                .PROVIDER_GIVEN_TRANSACTION_ID,
                                        "860c2f12-a275-4a40-82f3-a137cc3f1f63")
                                .build(),
                (Transaction)
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.inDKK(-111.99))
                                .setDescription("Transfer 2")
                                .setPending(false)
                                .setMutable(true)
                                .setDate(Date.from(Instant.parse("2019-10-30T00:00:00Z")))
                                .setTransactionDates(
                                        TransactionDates.builder()
                                                .setBookingDate(
                                                        new AvailableDateInformation()
                                                                .setDate(
                                                                        LocalDate.parse(
                                                                                "2019-10-30"))
                                                                .setInstant(
                                                                        Instant.parse(
                                                                                "2019-10-30T12:00:00Z")))
                                                .setValueDate(
                                                        new AvailableDateInformation()
                                                                .setDate(
                                                                        LocalDate.parse(
                                                                                "2019-10-30"))
                                                                .setInstant(null))
                                                .build())
                                .setTransactionReference("REF2")
                                .setProviderMarket("UK")
                                .addExternalSystemIds(
                                        TransactionExternalSystemIdType
                                                .PROVIDER_GIVEN_TRANSACTION_ID,
                                        "6d6dda69-0983-4bdc-aaa8-a452f7c2b899")
                                .build());
    }

    private Collection<Transaction> getExpectedLastPageTransactions() {
        return Arrays.asList(
                (Transaction)
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.inDKK(123.54))
                                .setDescription("Some transaction32")
                                .setPending(false)
                                .setMutable(true)
                                .setDate(Date.from(Instant.parse("2019-10-31T00:00:00Z")))
                                .setTransactionDates(
                                        TransactionDates.builder()
                                                .setBookingDate(
                                                        new AvailableDateInformation()
                                                                .setDate(
                                                                        LocalDate.parse(
                                                                                "2019-10-31"))
                                                                .setInstant(null))
                                                .setValueDate(
                                                        new AvailableDateInformation()
                                                                .setDate(
                                                                        LocalDate.parse(
                                                                                "2019-10-31"))
                                                                .setInstant(null))
                                                .build())
                                .setTransactionReference("REF3")
                                .setProviderMarket("UK")
                                .addExternalSystemIds(
                                        TransactionExternalSystemIdType
                                                .PROVIDER_GIVEN_TRANSACTION_ID,
                                        "e3fa564a-f0e0-41f6-972e-cb4aedb07d69")
                                .build(),
                (Transaction)
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.inDKK(-32.32))
                                .setDescription("Transfer 23")
                                .setPending(false)
                                .setMutable(true)
                                .setDate(Date.from(Instant.parse("2019-10-31T00:00:00Z")))
                                .setTransactionDates(
                                        TransactionDates.builder()
                                                .setBookingDate(
                                                        new AvailableDateInformation()
                                                                .setDate(
                                                                        LocalDate.parse(
                                                                                "2019-10-31"))
                                                                .setInstant(null))
                                                .setValueDate(
                                                        new AvailableDateInformation()
                                                                .setDate(
                                                                        LocalDate.parse(
                                                                                "2019-10-31"))
                                                                .setInstant(null))
                                                .build())
                                .setTransactionReference("REF4")
                                .setProviderMarket("UK")
                                .addExternalSystemIds(
                                        TransactionExternalSystemIdType
                                                .PROVIDER_GIVEN_TRANSACTION_ID,
                                        "7a0e6d1e-31a6-4324-b6d6-2a347cab91e2")
                                .build());
    }

    private TransactionalAccount getTestAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(123.45)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("UN123")
                                .withAccountNumber("AN123")
                                .withAccountName("NM123")
                                .addIdentifier(new OtherIdentifier("ID123"))
                                .build())
                .setApiIdentifier("BI123")
                .build()
                .orElse(null);
    }

    private HttpResponseException mockErrorResponse() {
        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponseException.getResponse().getStatus()).thenReturn(403);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "transactions_exception_response.json")
                                        .toFile(),
                                ErrorResponse.class));
        return httpResponseException;
    }

    private static class DelaySimulatingLocalDateTimeSource implements LocalDateTimeSource {

        private LocalDateTime currentTime;

        public DelaySimulatingLocalDateTimeSource(LocalDateTime currentTime) {
            this.currentTime = currentTime;
        }

        @Override
        public LocalDateTime now(ZoneId zoneId) {
            return currentTime;
        }

        @Override
        public Instant getInstant(ZoneId zoneId) {
            return null;
        }

        @Override
        public LocalDateTime now() {
            return currentTime;
        }

        @Override
        public Instant getInstant() {
            return null;
        }

        void increment() {
            this.currentTime = currentTime.plusSeconds(1);
        }
    }
}
