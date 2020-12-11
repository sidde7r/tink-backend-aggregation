package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class DanskeBankTransactionPaginatorTest {

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
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        AccountTransactionsV31Response::toAccountTransactionPaginationResponse,
                        localDateTimeSource);
    }

    @Test
    public void testRequestTimeProperlySaved() {
        TransactionalAccount account =
                TransactionalAccount.builder(
                                AccountTypes.CHECKING, "UN123", ExactCurrencyAmount.inEUR(123.45))
                        .setAccountNumber("AN123")
                        .setBankIdentifier("BI123")
                        .build();

        paginator.getTransactionsFor(account, null);

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
        when(persistentStorage.get("fetchedTxUntil:BI123")).thenReturn("2020-06-02T00:00:00");

        TransactionalAccount account =
                TransactionalAccount.builder(
                                AccountTypes.CHECKING, "UN123", ExactCurrencyAmount.inEUR(123.45))
                        .setAccountNumber("AN123")
                        .setBankIdentifier("BI123")
                        .build();

        paginator.getTransactionsFor(account, null);

        verify(apiClient)
                .fetchAccountTransactions(
                        eq("/some/path?fromBookingDateTime=2020-03-05T00:00:00Z"),
                        eq(AccountTransactionsV31Response.class));
    }

    private static class DelaySimulatingLocalDateTimeSource implements LocalDateTimeSource {

        private LocalDateTime currentTime;

        public DelaySimulatingLocalDateTimeSource(LocalDateTime currentTime) {
            this.currentTime = currentTime;
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
