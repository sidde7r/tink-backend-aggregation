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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactions.MonzoTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactions.MonzoTransactionPaginator;
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
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.identifiers.OtherIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.credentials.service.AccountTransactionsRefreshScope;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.TransactionsRefreshScope;
import se.tink.libraries.unleash.UnleashClient;

@RunWith(MockitoJUnitRunner.class)
public class MonzoTransactionPaginatorTest {

    private static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/monzo/resources/";

    @Mock private AgentComponentProvider componentProvider;
    @Mock private UnleashClient unleashClient;
    @Mock private CompositeAgentContext context;
    @Mock private Provider provider;

    private final DelaySimulatingLocalDateTimeSource localDateTimeSource =
            new DelaySimulatingLocalDateTimeSource(LocalDateTime.parse("2021-09-02T00:00:00"));

    private MonzoTransactionPaginator<AccountTransactionsV31Response, TransactionalAccount>
            paginator;
    private PersistentStorage persistentStorage;
    private UkOpenBankingApiClient apiClient;
    private CredentialsRequest request;
    private UkOpenBankingAisConfig ukOpenBankingAisConfig;

    @Before
    public void init() {
        ukOpenBankingAisConfig = mock(UkOpenBankingAisConfig.class);
        when(ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(any()))
                .thenReturn(String.format(ApiServices.ACCOUNT_TRANSACTIONS_REQUEST, "identifier1"));
        when(componentProvider.getContext()).thenReturn(context);
        when(context.getAppId()).thenReturn("mockedAppId");
        when(componentProvider.getUnleashClient()).thenReturn(unleashClient);
        when(provider.getName()).thenReturn("providerName");

        persistentStorage = mock(PersistentStorage.class);
        apiClient = mock(UkOpenBankingApiClient.class);
    }

    public void setBeforeTestWithCertainDate() {
        request = mock(CredentialsRequest.class);
        paginator =
                new MonzoTransactionPaginator(
                        componentProvider,
                        provider,
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

    public void setBeforeTestWithRefreshScope() {
        request = mock(RefreshInformationRequest.class);
        paginator =
                new MonzoTransactionPaginator(
                        componentProvider,
                        provider,
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
    public void shouldFetchOnePageOfTransactionsWhenThereIsNoKeyAndCertainDateIsNotPresent()
            throws IOException {
        // given
        setBeforeTestWithCertainDate();
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

    @Test
    public void shouldFetchTransactionsWhenCertainDateIsAfterFromDateForManualRefresh()
            throws IOException {
        // given
        setBeforeTestWithCertainDate();
        final Date youngCertainDate = new Date(1632913920000L); // 1632913920000L = 29-09-2021
        final AccountTransactionsV31Response response =
                loadSampleData(
                        "transactions_certain_date.json", AccountTransactionsV31Response.class);
        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(response);
        when(request.getAccounts()).thenReturn(createTestListAccounts(youngCertainDate));

        // when
        TransactionKeyPaginatorResponse<String> result =
                paginator.getTransactionsFor(createTestAccount(), null);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        new TransactionKeyPaginatorResponseImpl<String>(
                                getExpectedTransactionsWithBookedDate(), null));
    }

    @Test
    public void shouldFetchTransactionsWhenCertainDateIsBeforeThanFromDateForManualRefresh()
            throws IOException {
        // given
        setBeforeTestWithCertainDate();
        final Date oldCertainDate = new Date(1538219520000L); // 1538219520000L = 29-09-2018
        final AccountTransactionsV31Response response =
                loadSampleData("transactions.json", AccountTransactionsV31Response.class);
        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(response);
        when(request.getAccounts()).thenReturn(createTestListAccounts(oldCertainDate));
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

    @Test
    public void shouldFetchTransactionsWhenRefreshScopeIsEnabledForManualRefresh()
            throws IOException {
        // given
        setBeforeTestWithRefreshScope();
        final AccountTransactionsV31Response response =
                loadSampleData(
                        "transactions_certain_date.json", AccountTransactionsV31Response.class);
        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(response);
        when(((HasRefreshScope) request).getRefreshScope()).thenReturn(createRefreshScope());

        // when
        TransactionKeyPaginatorResponse<String> result =
                paginator.getTransactionsFor(createTestAccount(), null);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        new TransactionKeyPaginatorResponseImpl<String>(
                                getExpectedTransactionsWithBookedDate(), null));
    }

    @Test
    public void shouldFetchTransactionsWhenRefreshScopeIsEnabledWithAccountHistoryDate()
            throws IOException {
        // given
        setBeforeTestWithRefreshScope();
        final AccountTransactionsV31Response response =
                loadSampleData(
                        "transactions_certain_date.json", AccountTransactionsV31Response.class);
        final LocalDate localDate = LocalDate.of(2021, 10, 1);
        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(response);
        when(((HasRefreshScope) request).getRefreshScope())
                .thenReturn(createRefreshScope(true, localDate));

        // when
        TransactionKeyPaginatorResponse<String> result =
                paginator.getTransactionsFor(createTestAccount(), null);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(
                        new TransactionKeyPaginatorResponseImpl<String>(
                                getExpectedTransactionsWithBookedDate(), null));
    }

    private RefreshScope createRefreshScope() {
        return createRefreshScope(false, null);
    }

    private RefreshScope createRefreshScope(boolean isAccountShouldBeAdded, LocalDate date) {
        RefreshScope refreshScope = new RefreshScope();
        refreshScope.setTransactions(createTransactionsRefreshScope(isAccountShouldBeAdded, date));
        return refreshScope;
    }

    private TransactionsRefreshScope createTransactionsRefreshScope(
            boolean isAccountShouldBeAdded, LocalDate bookedTransactionsDate) {
        LocalDate defaultRefreshScopeDate = LocalDate.of(2020, 10, 2);
        TransactionsRefreshScope transactionsRefreshScope = new TransactionsRefreshScope();
        transactionsRefreshScope.setTransactionBookedDateGte(defaultRefreshScopeDate);

        if (isAccountShouldBeAdded) {
            AccountTransactionsRefreshScope account = new AccountTransactionsRefreshScope();
            account.setAccountIdentifiers(
                    Stream.of(new OtherIdentifier("id1").toString()).collect(Collectors.toSet()));
            account.setTransactionBookedDateGte(bookedTransactionsDate);
            Set<AccountTransactionsRefreshScope> accounts =
                    Stream.of(account).collect(Collectors.toSet());
            transactionsRefreshScope.setAccounts(accounts);
        }
        return transactionsRefreshScope;
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

    private List<Account> createTestListAccounts(Date certainDate) {
        Account account = new Account();
        account.setBankId("Unique1");
        account.setCertainDate(certainDate);
        return Collections.singletonList(account);
    }

    private Collection<Transaction> getExpectedLastPageTransactions() {
        return Collections.singletonList(
                (Transaction)
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.of(-18.9600, "GBP"))
                                .setDescription("RAW")
                                .setPending(true)
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

    private Collection<Transaction> getExpectedTransactionsWithBookedDate() {
        return Collections.singletonList(
                (Transaction)
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.of(-18.9600, "GBP"))
                                .setDescription("RAW")
                                .setPending(true)
                                .setDate(Date.from(Instant.parse("2021-10-01T17:20:19.485Z")))
                                .setMerchantCategoryCode("5812")
                                .setProprietaryFinancialInstitutionType("mastercard")
                                .setTransactionDates(
                                        TransactionDates.builder()
                                                .setBookingDate(
                                                        new AvailableDateInformation()
                                                                .setDate(
                                                                        LocalDate.parse(
                                                                                "2021-10-01"))
                                                                .setInstant(
                                                                        Instant.parse(
                                                                                "2021-10-01T17:20:19.485Z")))
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
        public LocalDateTime now() {
            return currentTime;
        }

        @Override
        public Instant getInstant() {
            return currentTime.toInstant(ZoneOffset.UTC);
        }
    }
}
