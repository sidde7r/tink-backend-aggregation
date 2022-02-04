package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher;

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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.monzo.mapper.MonzoTransactionMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.RefreshScopeTransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
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
import se.tink.libraries.unleash.UnleashClient;

@RunWith(MockitoJUnitRunner.class)
public class UkObReverseTransactionPaginatorTest {

    private static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base/fetcher/resources/";

    private final DelaySimulatingLocalDateTimeSource localDateTimeSource =
            new DelaySimulatingLocalDateTimeSource(LocalDateTime.parse("2021-10-02T00:00:00"));

    @Mock private AgentComponentProvider componentProvider;
    @Mock private UnleashClient unleashClient;
    @Mock private CompositeAgentContext context;
    @Mock private Provider provider;

    private AccountTransactionsV31Response oldTransactions;
    private AccountTransactionsV31Response currentTransactions;
    private PersistentStorage persistentStorage;
    private UkOpenBankingApiClient apiClient;
    private UkOpenBankingAisConfig ukOpenBankingAisConfig;
    private UkOpenBankingTransactionPaginator<AccountTransactionsV31Response, TransactionalAccount>
            transactionPaginator;
    private TransactionPaginationHelper paginationHelper;

    @Before
    public void init() throws IOException {
        oldTransactions =
                loadSampleData("two-years-transactions.json", AccountTransactionsV31Response.class);
        currentTransactions =
                loadSampleData("last-transactions.json", AccountTransactionsV31Response.class);

        persistentStorage = mock(PersistentStorage.class);
        apiClient = mock(UkOpenBankingApiClient.class);
        ukOpenBankingAisConfig = mock(UkOpenBankingAisConfig.class);
        when(ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(any()))
                .thenReturn(String.format(ApiServices.ACCOUNT_TRANSACTIONS_REQUEST, "identifier1"));
        when(componentProvider.getContext()).thenReturn(context);
        when(context.getAppId()).thenReturn("mockedAppId");
        when(componentProvider.getUnleashClient()).thenReturn(unleashClient);
        when(provider.getName()).thenReturn("providerName");

        paginationHelper = mock(RefreshScopeTransactionPaginationHelper.class);
        transactionPaginator =
                new UkOpenBankingTransactionPaginator<>(
                        componentProvider,
                        provider,
                        ukOpenBankingAisConfig,
                        persistentStorage,
                        apiClient,
                        AccountTransactionsV31Response.class,
                        (response, account) ->
                                AccountTransactionsV31Response
                                        .toAccountTransactionPaginationResponse(
                                                response, new MonzoTransactionMapper()),
                        localDateTimeSource,
                        paginationHelper);
    }

    @Test
    public void
            shouldFetchOnePageOfTransactionsWhenThereIsNoKeyAndLastTransactionsDateIsNotPresent() {
        // given
        final Instant oldInstant = Instant.parse("2021-07-12T17:20:19.485Z");
        final LocalDate oldLocalDate = LocalDate.parse("2021-07-12");
        final Collection<Transaction> expected =
                getExpectedPageWithTransactions(oldInstant, oldLocalDate);
        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(oldTransactions);

        // when
        TransactionKeyPaginatorResponse<String> result =
                transactionPaginator.getTransactionsFor(createTestAccount(), null);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(new TransactionKeyPaginatorResponseImpl<String>(expected, null));
    }

    @Test
    public void shouldFetchTransactionsWhenLastTransactionsDateIsBeforeFromBookingDate() {
        // given
        final Instant oldInstant = Instant.parse("2021-07-12T17:20:19.485Z");
        final LocalDate oldLocalDate = LocalDate.parse("2021-07-12");
        final Collection<Transaction> expected =
                getExpectedPageWithTransactions(oldInstant, oldLocalDate);
        final Optional<Date> oldCertainDate =
                Optional.of(new Date(1538219520000L)); // 1538219520000L = 29-09-2018
        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(oldTransactions);
        when(paginationHelper.getTransactionDateLimit(any())).thenReturn(oldCertainDate);

        // when
        TransactionKeyPaginatorResponse<String> result =
                transactionPaginator.getTransactionsFor(createTestAccount(), null);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(new TransactionKeyPaginatorResponseImpl<String>(expected, null));
    }

    @Test
    public void shouldFetchTransactionsWhenLastTransactionsDateIsAfterFromBookingDate() {
        // given
        final Instant currentInstant = Instant.parse("2021-10-01T17:20:19.485Z");
        final LocalDate currentLocalDate = LocalDate.parse("2021-10-01");
        final Collection<Transaction> expected =
                getExpectedPageWithTransactions(currentInstant, currentLocalDate);
        final Optional<Date> youngCertainDate =
                Optional.of(new Date(1632913920000L)); // 1632913920000L = 29-09-2021
        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(currentTransactions);
        when(paginationHelper.getTransactionDateLimit(any())).thenReturn(youngCertainDate);

        // when
        TransactionKeyPaginatorResponse<String> result =
                transactionPaginator.getTransactionsFor(createTestAccount(), null);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(new TransactionKeyPaginatorResponseImpl<String>(expected, null));
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

    private Collection<Transaction> getExpectedPageWithTransactions(
            Instant instantDate, LocalDate localDate) {
        return Collections.singletonList(
                (Transaction)
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.of(-18.9600, "GBP"))
                                .setDescription("RAW")
                                .setPending(true)
                                .setDate(Date.from(instantDate))
                                .setMerchantCategoryCode("5812")
                                .setProprietaryFinancialInstitutionType("mastercard")
                                .setTransactionDates(
                                        TransactionDates.builder()
                                                .setBookingDate(
                                                        new AvailableDateInformation()
                                                                .setDate(localDate)
                                                                .setInstant(instantDate))
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

        private DelaySimulatingLocalDateTimeSource(LocalDateTime currentTime) {
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
            return currentTime.toInstant(ZoneOffset.UTC);
        }
    }
}
