package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.RefreshScopeTransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.identifiers.OtherIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.unleash.UnleashClient;

@RunWith(MockitoJUnitRunner.class)
public class UkOpenBankingTransactionPaginatorTest {

    @Mock AgentComponentProvider componentProvider;
    @Mock UnleashClient unleashClient;
    @Mock CompositeAgentContext context;
    @Mock Provider provider;

    private static final TransactionalAccount TRANSACTIONAL_ACCOUNT =
            TransactionalAccount.nxBuilder()
                    .withType(TransactionalAccountType.CHECKING)
                    .withPaymentAccountFlag()
                    .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(123.45)))
                    .withId(
                            IdModule.builder()
                                    .withUniqueIdentifier("UN123")
                                    .withAccountNumber("UN123")
                                    .withAccountName("AN123")
                                    .addIdentifier(new OtherIdentifier("ID123"))
                                    .build())
                    .setApiIdentifier("BI123")
                    .build()
                    .orElse(null);

    private UkOpenBankingTransactionPaginator<AccountTransactionsV31Response, TransactionalAccount>
            paginator;
    private PersistentStorage persistentStorage;
    private UkOpenBankingApiClient apiClient;
    private TransactionPaginationHelper paginationHelper;
    private final ArgumentCaptor<String> persistentStorageCaptor =
            ArgumentCaptor.forClass(String.class);

    @Before
    public void setup() {
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
        paginationHelper = mock(RefreshScopeTransactionPaginationHelper.class);
        paginator =
                new UkOpenBankingTransactionPaginator<>(
                        componentProvider,
                        provider,
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        (response, account) ->
                                AccountTransactionsV31Response
                                        .toAccountTransactionPaginationResponse(response),
                        localDateTimeSource,
                        paginationHelper);
    }

    @Test
    public void testRequestTimeProperlySaved() {
        when(unleashClient.isToggleEnabled(Mockito.any())).thenReturn(false);

        paginator.getTransactionsFor(TRANSACTIONAL_ACCOUNT, null);

        verify(persistentStorage)
                .put(eq("fetchedTxUntil:BI123"), persistentStorageCaptor.capture());

        verify(apiClient)
                .fetchAccountTransactions(
                        eq("/some/path?fromBookingDateTime=2018-07-02T00:00:00Z"),
                        eq(AccountTransactionsV31Response.class));

        assertEquals("2020-06-02T00:00:00", persistentStorageCaptor.getValue());
    }

    @Test
    public void testFetchTransactionsForShortPeriod() {
        when(unleashClient.isToggleEnabled(Mockito.any())).thenReturn(false);
        when(persistentStorage.get("fetchedTxUntil:BI123")).thenReturn("2020-06-02T10:10:10");

        paginator.getTransactionsFor(TRANSACTIONAL_ACCOUNT, null);

        verify(apiClient)
                .fetchAccountTransactions(
                        eq("/some/path?fromBookingDateTime=2020-03-05T00:00:00Z"),
                        eq(AccountTransactionsV31Response.class));
    }

    @Test
    public void shouldRecoverResponseWhen403CodeAppearedAndTryToFetchTransactionsFromLast89Days() {
        // given
        HttpResponse http403Response = mock(HttpResponse.class);
        when(http403Response.getStatus()).thenReturn(403);

        // when
        when(apiClient.fetchAccountTransactions(any(), any()))
                .thenThrow(new HttpResponseException(mock(HttpRequest.class), http403Response))
                .thenReturn(new AccountTransactionsV31Response());

        // then
        Assertions.assertThatCode(() -> paginator.getTransactionsFor(TRANSACTIONAL_ACCOUNT, null))
                .doesNotThrowAnyException();
    }

    @Test
    public void getTransactionsForShouldFetch24MonthsInstead23WhenUnleashIsActive() {
        when(unleashClient.isToggleEnabled(Mockito.any())).thenReturn(true);

        paginator.getTransactionsFor(TRANSACTIONAL_ACCOUNT, null);

        verify(apiClient)
                .fetchAccountTransactions(
                        eq("/some/path?fromBookingDateTime=2018-06-02T00:00:00Z"), // 24 months
                        eq(AccountTransactionsV31Response.class));
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
        public ZonedDateTime nowZonedDateTime(ZoneId zoneId) {
            return null;
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
