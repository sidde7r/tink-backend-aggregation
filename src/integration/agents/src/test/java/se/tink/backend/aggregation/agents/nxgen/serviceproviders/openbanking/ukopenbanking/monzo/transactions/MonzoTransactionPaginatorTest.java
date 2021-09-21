package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.monzo.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactions.MonzoTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactions.MonzoTransactionPaginator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.identifiers.OtherIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MonzoTransactionPaginatorTest {

    private static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/monzo/resources/";

    private MonzoTransactionPaginator<AccountTransactionsV31Response, TransactionalAccount>
            paginator;
    private PersistentStorage persistentStorage;
    private UkOpenBankingApiClient apiClient;
    private CredentialsRequest request;

    @Before
    public void init() {
        UkOpenBankingAisConfig ukOpenBankingAisConfig = mock(UkOpenBankingAisConfig.class);

        DelaySimulatingLocalDateTimeSource localDateTimeSource =
                new DelaySimulatingLocalDateTimeSource(LocalDateTime.parse("2021-09-02T00:00:00"));

        persistentStorage = mock(PersistentStorage.class);
        request = mock(CredentialsRequest.class);
        apiClient = mock(UkOpenBankingApiClient.class);

        paginator =
                new MonzoTransactionPaginator(
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        (response, account) ->
                                AccountTransactionsV31Response
                                        .toAccountTransactionPaginationResponse(
                                                (AccountTransactionsV31Response) response,
                                                new MonzoTransactionMapper()),
                        localDateTimeSource,
                        request);
    }

    @Test
    public void shouldFetchOnePageOfTransactionsWhenThereIsNoKey() throws IOException {
        // given
        final AccountTransactionsV31Response response =
                loadSampleData("transactions.json", AccountTransactionsV31Response.class);
        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(response);

        // when
        TransactionKeyPaginatorResponse<String> result =
                paginator.getTransactionsFor(createTestAccount(), null);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        new TransactionKeyPaginatorResponseImpl<String>(
                                getExpectedLastPageTransactions(), null));
    }

    private TransactionalAccount createTestAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(123.45)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("Unique1")
                                .withAccountNumber("acc1")
                                .withAccountName("name1")
                                .addIdentifier(new OtherIdentifier("id1"))
                                .build())
                .setApiIdentifier("identifier1")
                .build()
                .orElse(null);
    }

    private Collection<Transaction> getExpectedLastPageTransactions() {
        return Collections.singletonList(
                (Transaction)
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.of(-18.9600, "GBP"))
                                .setDescription("RAW")
                                .setPending(true)
                                .setMutable(true)
                                .setDate(Date.from(Instant.parse("2021-07-12T17:20:19.485Z")))
                                .setMerchantCategoryCode("5812")
                                .setProprietaryFinancialInstitutionType("mastercard")
                                .setTransactionDates(
                                        TransactionDates.builder()
                                                .setBookingDate(
                                                        new AvailableDateInformation()
                                                                .setDate(
                                                                        LocalDate.parse(
                                                                                "2021-07-12"))
                                                                .setInstant(
                                                                        Instant.parse(
                                                                                "2021-07-12T17:20:19.485Z")))
                                                .build())
                                .setProviderMarket("UK")
                                .addExternalSystemIds(
                                        TransactionExternalSystemIdType
                                                .PROVIDER_GIVEN_TRANSACTION_ID,
                                        "tx_0000A8DTP")
                                .build());
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }

    private static class DelaySimulatingLocalDateTimeSource implements LocalDateTimeSource {

        private final LocalDateTime currentTime;

        public DelaySimulatingLocalDateTimeSource(LocalDateTime currentTime) {
            this.currentTime = currentTime;
        }

        @Override
        public LocalDateTime now() {
            return currentTime;
        }

        @Override
        public Instant getInstant() {
            return currentTime.toInstant(ZoneOffset.UTC);
        }
    }
}
