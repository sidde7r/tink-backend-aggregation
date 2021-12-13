package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter.BelfiusClientConfigurator;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.to_system.AccountConverter;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;

@RunWith(JUnitParamsRunner.class)
public class BelfiusTransactionalAccountRefreshControllerTest {

    private static final String BASE_RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/belfius/";
    private static final String RESOURCES_PATH = BASE_RESOURCES_PATH + "resources/";
    private static final String TEST_LOGICAL_ID = "SOME_LOGICAL_ID";
    private static final String TEST_USER = "USER_NAME";
    private static final int TEST_RETRY_SLEEP_MS = 1;
    private static final int TEST_MAX_RETRIES_NUMBER = 1;
    private static final Date SESSION_EXPIRY_DATE =
            new Date(new ConstantLocalDateTimeSource().getSystemCurrentTimeMillis());

    private static final FetchAccountResponse FETCH_ACCOUNT_RESPONSE =
            deserializeFromFile(
                    RESOURCES_PATH + "accounts_response.json", FetchAccountResponse.class);
    private static final FetchAccountResponse FETCH_ACCOUNT_WITHOUT_OWNER_RESPONSE =
            deserializeFromFile(
                    RESOURCES_PATH + "accounts_response_no_owner.json", FetchAccountResponse.class);
    private static final String FIRST_PAGE_TRANSACTIONS_STRING =
            readFileToString(RESOURCES_PATH + "transactions_response_first_page.json");
    private static final String LAST_PAGE_TRANSACTIONS_STRING =
            readFileToString(RESOURCES_PATH + "transactions_response_last_page.json");

    @Mock private MetricRefreshController metricRefreshController;
    @Mock private Filter callFilter;
    @Mock private HttpResponse response;

    private final TinkHttpClient client =
            NextGenTinkHttpClient.builder(
                            new FakeLogMasker(),
                            LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                    .build();
    private final BelfiusConfiguration belfiusConfiguration =
            deserializeFromFile(
                    RESOURCES_PATH + "belfius_configuration.json", BelfiusConfiguration.class);
    private final PersistentStorage persistentStorage = new PersistentStorage();
    private final Provider provider = new Provider();
    private final User user = new User();

    private TransactionalAccountRefreshController refreshController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        AgentConfiguration<BelfiusConfiguration> agentConfiguration =
                new AgentConfiguration.Builder<BelfiusConfiguration>()
                        .setProviderSpecificConfiguration(belfiusConfiguration)
                        .setRedirectUrl("https://api.tink.test")
                        .build();

        clientIsAuthenticated();
        configureProvider();

        BelfiusApiClient apiClient = getConfiguredBelfiusApiClient(agentConfiguration);
        BelfiusTransactionalAccountFetcher accountTransactionFetcher =
                new BelfiusTransactionalAccountFetcher(apiClient, persistentStorage);

        refreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        new UpdateController(provider, user),
                        accountTransactionFetcher,
                        new TransactionKeyWithInitDateFromFetcherController<>(
                                new RefreshInformationRequest(), accountTransactionFetcher));

        defaultMockConfiguration();
    }

    private void clientIsAuthenticated() {
        persistentStorage.put(StorageKeys.LOGICAL_ID, TEST_LOGICAL_ID);
        persistentStorage.put(
                StorageKeys.OAUTH_TOKEN,
                OAuth2Token.create("bearer", "test_access_token", "test_refresh_token", 899));
    }

    private void configureProvider() {
        provider.setCurrency("EUR");
        provider.setMarket("BE");
    }

    private BelfiusApiClient getConfiguredBelfiusApiClient(
            AgentConfiguration<BelfiusConfiguration> agentConfiguration) {
        new BelfiusClientConfigurator(new ConstantLocalDateTimeSource())
                .configure(
                        client,
                        persistentStorage,
                        TEST_RETRY_SLEEP_MS,
                        TEST_MAX_RETRIES_NUMBER,
                        SESSION_EXPIRY_DATE);
        client.addFilter(callFilter);

        return new BelfiusApiClient(client, agentConfiguration, new MockRandomValueGenerator());
    }

    private void defaultMockConfiguration() {
        given(metricRefreshController.buildAction(any(), any()))
                .willReturn(mock(MetricRefreshAction.class));
        given(callFilter.handle(any())).willReturn(response);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(FetchAccountResponse.class)).willReturn(FETCH_ACCOUNT_RESPONSE);
    }

    @Test
    public void shouldFetchAccounts() {
        // when
        FetchAccountsResponse result = refreshController.fetchCheckingAccounts();

        // then
        assertEqualsIgnoringIds(result, getExpectedAccountsResponse(TEST_USER));
    }

    @Test
    public void shouldFetchAccountsWhenAccountOwnerIsNotPresent() {
        // given
        given(response.getBody(FetchAccountResponse.class))
                .willReturn(FETCH_ACCOUNT_WITHOUT_OWNER_RESPONSE);

        // when
        FetchAccountsResponse result = refreshController.fetchCheckingAccounts();

        // then
        assertEqualsIgnoringIds(result, getExpectedAccountsResponse(null));
    }

    @Test
    public void shouldFetchTransactionsWithoutPagination() {
        // given
        given(response.getBody(String.class)).willReturn(LAST_PAGE_TRANSACTIONS_STRING);

        // when
        FetchTransactionsResponse result = refreshController.fetchCheckingTransactions();

        // then
        assertEqualsIgnoringIds(result, getExpectedTransactionsResponse(getLastPageTransactions()));
    }

    @Test
    public void shouldFetchTransactionsWithPagination() {
        // given
        given(response.getBody(String.class))
                .willReturn(FIRST_PAGE_TRANSACTIONS_STRING)
                .willReturn(LAST_PAGE_TRANSACTIONS_STRING);

        // when
        FetchTransactionsResponse result = refreshController.fetchCheckingTransactions();

        // then
        assertEqualsIgnoringIds(
                result,
                getExpectedTransactionsResponse(
                        ListUtils.union(getFirstPageTransactions(), getLastPageTransactions())));
    }

    @Test
    @Parameters({"500", "501", "502", "503", "555"})
    public void shouldThrowOnBankSideErrorWhileFetchingAccounts(int statusCode) {
        // given
        given(response.getStatus()).willReturn(statusCode);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Http status: " + statusCode);
    }

    @Test
    @Parameters({"500", "501", "502", "503", "555"})
    public void shouldThrowOnBankSideErrorWhileFetchingTransactions(int statusCode) {
        // given
        FetchAccountsResponse result = refreshController.fetchCheckingAccounts();
        given(response.getStatus()).willReturn(statusCode);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingTransactions())
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Http status: " + statusCode);

        // and
        assertEqualsIgnoringIds(result, getExpectedAccountsResponse(TEST_USER));
    }

    @Test
    public void shouldThrowAccountRefreshExceptionWhenBankRespondsWithError() {
        // given
        bankRespondsWithErrorAccountNotSupported();

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .isInstanceOf(AccountRefreshException.class)
                .hasMessageContaining("This account is not allowed for this type of request.");
    }

    @Test
    @Parameters
    public void shouldRetryOnErrorsSuccessfully(String message) {
        // given
        given(callFilter.handle(any()))
                .willThrow(new HttpClientException(message, null))
                .willReturn(response);

        // when
        FetchAccountsResponse result = refreshController.fetchCheckingAccounts();

        // then
        assertEqualsIgnoringIds(result, getExpectedAccountsResponse(TEST_USER));
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldRetryOnErrorsSuccessfully() {
        return new Object[] {
            new Object[] {"Remote host terminated the handshake"},
            new Object[] {"connect timed out"},
            new Object[] {"connection reset"},
        };
    }

    @Test
    @Parameters(method = "failedRetryErrorParams")
    public void shouldThrowAfterRetries(String message, RuntimeException expected) {
        // given
        given(callFilter.handle(any())).willThrow(new HttpClientException(message, null));

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .isInstanceOf(expected.getClass())
                .hasMessage(expected.getMessage());
    }

    @Test
    @Parameters(method = "failedRetryErrorParams")
    public void shouldThrowAfterRetriesWhileFetchingTransactions(
            String message, RuntimeException expected) {
        // given
        FetchAccountsResponse result = refreshController.fetchCheckingAccounts();
        given(callFilter.handle(any())).willThrow(new HttpClientException(message, null));

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingTransactions())
                .isInstanceOf(expected.getClass())
                .hasMessage(expected.getMessage());

        // and
        assertEqualsIgnoringIds(result, getExpectedAccountsResponse(TEST_USER));
    }

    @SuppressWarnings("unused")
    private Object[] failedRetryErrorParams() {
        return new Object[] {
            new Object[] {
                "Remote host terminated the handshake",
                new HttpClientException("Remote host terminated the handshake", null)
            },
            new Object[] {"connect timed out", BankServiceError.BANK_SIDE_FAILURE.exception()},
            new Object[] {"connection reset", BankServiceError.BANK_SIDE_FAILURE.exception()},
        };
    }

    private void bankRespondsWithErrorAccountNotSupported() {
        given(response.getStatus()).willReturn(403);
        given(response.getBody(ErrorResponse.class))
                .willReturn(
                        deserializeFromFile(
                                BASE_RESOURCES_PATH
                                        + "authenticator/resources/belfius_account_not_supported_error.json",
                                ErrorResponse.class));
    }

    private <T> void assertEqualsIgnoringIds(T result, T expected) {
        assertThat(result)
                .usingRecursiveComparison()
                // id field is randomly generated UUID
                .ignoringFields("accounts.id", "transactions.id")
                .isEqualTo(expected);
    }

    private FetchAccountsResponse getExpectedAccountsResponse(String holderName) {
        TransactionalAccount transactionalAccount =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.CHECKING)
                        .withPaymentAccountFlag()
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(789.01)))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("BE39000000076000")
                                        .withAccountNumber("BE39000000076000")
                                        .withAccountName("DUMMY_NAME")
                                        .addIdentifier(new IbanIdentifier("BE39000000076000"))
                                        .build())
                        .addHolderName(holderName)
                        .setApiIdentifier(TEST_LOGICAL_ID)
                        .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                        .build()
                        .orElseThrow(IllegalStateException::new);

        return new FetchAccountsResponse(
                Collections.singletonList(
                        AccountConverter.toSystemAccount(user, provider, transactionalAccount)));
    }

    private FetchTransactionsResponse getExpectedTransactionsResponse(
            List<se.tink.backend.aggregation.agents.models.Transaction> transactions) {
        return new FetchTransactionsResponse(
                Collections.singletonMap(
                        getExpectedAccountsResponse(TEST_USER).getAccounts().get(0), transactions));
    }

    private List<se.tink.backend.aggregation.agents.models.Transaction> getFirstPageTransactions() {
        return Arrays.asList(
                Transaction.builder()
                        .setDescription("SEPA CREDIT TRANSFER from PSD2Company")
                        .setAmount(ExactCurrencyAmount.of(12.25, provider.getCurrency()))
                        .setDate(new Date(1596067200000L))
                        .build()
                        .toSystemTransaction(false),
                Transaction.builder()
                        .setDescription("SEPA CREDIT TRANSFER to somewhere")
                        .setAmount(ExactCurrencyAmount.of(-10, provider.getCurrency()))
                        .setDate(new Date(1596153600000L))
                        .build()
                        .toSystemTransaction(false));
    }

    private List<se.tink.backend.aggregation.agents.models.Transaction> getLastPageTransactions() {
        return Collections.singletonList(
                Transaction.builder()
                        .setDescription("SEPA CREDIT TRANSFER1")
                        .setAmount(ExactCurrencyAmount.of(99.99, provider.getCurrency()))
                        .setDate(new Date(1597449600000L))
                        .build()
                        .toSystemTransaction(false));
    }

    private static <T> T deserializeFromFile(String filePath, Class<T> clazz) {
        return SerializationUtils.deserializeFromString(Paths.get(filePath).toFile(), clazz);
    }

    private static String readFileToString(String filePath) {
        try {
            return FileUtils.readFileToString(Paths.get(filePath).toFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
