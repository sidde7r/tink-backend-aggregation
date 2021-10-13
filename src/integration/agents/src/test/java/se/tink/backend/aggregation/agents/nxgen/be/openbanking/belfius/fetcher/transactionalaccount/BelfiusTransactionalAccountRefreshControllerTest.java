package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusClientConfigurer;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
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

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/belfius/resources/";
    private static final String CONFIGURATION_FILE_PATH =
            RESOURCES_PATH + "belfius_configuration.json";
    private static final String ACCOUNTS_FILE_PATH = RESOURCES_PATH + "accounts_response.json";
    private static final String ACCOUNTS_NO_OWNER_FILE_PATH =
            RESOURCES_PATH + "accounts_response_no_owner.json";
    private static final String TRANSACTIONS_LAST_PAGE_FILE_PATH =
            RESOURCES_PATH + "transactions_response_last_page.json";
    private static final String TRANSACTIONS_FIRST_PAGE_FILE_PATH =
            RESOURCES_PATH + "transactions_response_first_page.json";

    private static final String REDIRECT_URL = "https://api.tink.test";
    private static final String TEST_LOGICAL_ID = "SOME_LOGICAL_ID";
    private static final OAuth2Token TEST_TOKEN =
            OAuth2Token.create("bearer", "test_access_token", "test_refresh_token", 899);
    private static final String TEST_USER = "USER_NAME";
    private static final String CURRENCY = "EUR";
    private static final int TEST_RETRY_SLEEP_MS = 100;

    private static final FetchAccountResponse FETCH_ACCOUNT_RESPONSE =
            deserializeFromFile(ACCOUNTS_FILE_PATH, FetchAccountResponse.class);
    private static final FetchAccountResponse FETCH_ACCOUNT_WITHOUT_OWNER_RESPONSE =
            deserializeFromFile(ACCOUNTS_NO_OWNER_FILE_PATH, FetchAccountResponse.class);
    private static final String FIRST_PAGE_TRANSACTIONS_STRING =
            readFileToString(TRANSACTIONS_FIRST_PAGE_FILE_PATH);
    private static final String LAST_PAGE_TRANSACTIONS_STRING =
            readFileToString(TRANSACTIONS_LAST_PAGE_FILE_PATH);

    @Mock private MetricRefreshController metricRefreshController;
    @Mock private MetricRefreshAction metricRefreshAction;
    @Mock private Filter nextFilter;
    @Mock private HttpResponse response;

    private final TinkHttpClient client =
            NextGenTinkHttpClient.builder(
                            new FakeLogMasker(),
                            LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                    .build();
    private final BelfiusConfiguration belfiusConfiguration =
            deserializeFromFile(CONFIGURATION_FILE_PATH, BelfiusConfiguration.class);
    private final PersistentStorage persistentStorage = new PersistentStorage();
    private final RandomValueGenerator randomValueGenerator = new MockRandomValueGenerator();
    private final Provider provider = new Provider();
    private final User user = new User();

    private TransactionalAccountRefreshController refreshController;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        AgentConfiguration<BelfiusConfiguration> agentConfiguration =
                new AgentConfiguration.Builder<BelfiusConfiguration>()
                        .setProviderSpecificConfiguration(belfiusConfiguration)
                        .setRedirectUrl(REDIRECT_URL)
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

        setDefaultMocks();
    }

    private void clientIsAuthenticated() {
        persistentStorage.put(StorageKeys.LOGICAL_ID, TEST_LOGICAL_ID);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, TEST_TOKEN);
    }

    private void configureProvider() {
        provider.setCurrency(CURRENCY);
        provider.setMarket("BE");
    }

    private BelfiusApiClient getConfiguredBelfiusApiClient(
            AgentConfiguration<BelfiusConfiguration> agentConfiguration) {
        new BelfiusClientConfigurer()
                .withRetrySleepMilliseconds(TEST_RETRY_SLEEP_MS)
                .configure(client, persistentStorage);
        client.addFilter(nextFilter);

        return new BelfiusApiClient(client, agentConfiguration, randomValueGenerator);
    }

    private void setDefaultMocks() {
        given(metricRefreshController.buildAction(any(), any())).willReturn(metricRefreshAction);
        given(nextFilter.handle(any())).willReturn(response);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(FetchAccountResponse.class)).willReturn(FETCH_ACCOUNT_RESPONSE);
    }

    @Test
    public void shouldFetchAccounts() {
        // given & when
        FetchAccountsResponse result = refreshController.fetchCheckingAccounts();

        // then
        assertResultEqualsExpected(result, getExpectedAccountsResponse(TEST_USER), "accounts.id");
    }

    @Test
    public void shouldFetchAccountsWhenAccountOwnerIsNotPresent() {
        // given
        given(response.getBody(FetchAccountResponse.class))
                .willReturn(FETCH_ACCOUNT_WITHOUT_OWNER_RESPONSE);

        // when
        FetchAccountsResponse result = refreshController.fetchCheckingAccounts();

        // then
        assertResultEqualsExpected(result, getExpectedAccountsResponse(null), "accounts.id");
    }

    @Test
    public void shouldFetchTransactionsWithoutPagination() {
        // given
        given(response.getBody(String.class)).willReturn(LAST_PAGE_TRANSACTIONS_STRING);

        // when
        FetchTransactionsResponse result = refreshController.fetchCheckingTransactions();

        // then
        assertResultEqualsExpected(
                result,
                getExpectedTransactionsResponse(getLastPageTransactions()),
                "transactions.id");
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
        assertResultEqualsExpected(
                result,
                getExpectedTransactionsResponse(
                        ListUtils.union(getFirstPageTransactions(), getLastPageTransactions())),
                "transactions.id");
    }

    @Test
    @Parameters({"500", "501", "502", "503", "555"})
    public void shouldThrowOnBankSideErrorWhileFetchingAccounts(int statusCode) {
        // given
        given(response.getStatus()).willReturn(statusCode);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .usingRecursiveComparison()
                .isEqualTo(BankServiceError.NO_BANK_SERVICE.exception());
    }

    @Test
    @Parameters({"500", "501", "502", "503", "555"})
    public void shouldThrowOnBankSideErrorWhileFetchingTransactions(int statusCode) {
        // given
        FetchAccountsResponse result = refreshController.fetchCheckingAccounts();
        given(response.getStatus()).willReturn(statusCode);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingTransactions())
                .usingRecursiveComparison()
                .isEqualTo(BankServiceError.NO_BANK_SERVICE.exception());

        // and
        assertResultEqualsExpected(result, getExpectedAccountsResponse(TEST_USER), "accounts.id");
    }

    @Test
    @Parameters(method = "successRetryErrorParams")
    public void shouldRetryOnErrorsSuccessfully(RuntimeException thrown) {
        // given
        given(nextFilter.handle(any())).willThrow(thrown).willReturn(response);

        // when
        FetchAccountsResponse result = refreshController.fetchCheckingAccounts();

        // then
        assertResultEqualsExpected(result, getExpectedAccountsResponse(TEST_USER), "accounts.id");
    }

    private Object[] successRetryErrorParams() {
        return new Object[] {
            new Object[] {new HttpClientException("Remote host terminated the handshake", null)},
            new Object[] {new HttpClientException("connect timed out", null)},
            new Object[] {new HttpClientException("connection reset", null)},
        };
    }

    @Test
    @Parameters(method = "failedRetryErrorParams")
    public void shouldThrowAfterRetries(RuntimeException thrown, RuntimeException expected) {
        // given
        given(nextFilter.handle(any())).willThrow(thrown);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .usingRecursiveComparison()
                .isEqualTo(expected);

        // and
        assertThatThrownBy(() -> refreshController.fetchCheckingTransactions())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @Parameters(method = "failedRetryErrorParams")
    public void shouldThrowAfterRetriesWhileFetchingTransactions(
            RuntimeException thrown, RuntimeException expected) {
        // given
        FetchAccountsResponse result = refreshController.fetchCheckingAccounts();
        given(nextFilter.handle(any())).willThrow(thrown);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingTransactions())
                .usingRecursiveComparison()
                .isEqualTo(expected);

        // and
        assertResultEqualsExpected(result, getExpectedAccountsResponse(TEST_USER), "accounts.id");
    }

    private Object[] failedRetryErrorParams() {
        return new Object[] {
            new Object[] {
                new HttpClientException("Remote host terminated the handshake", null),
                new HttpClientException("Remote host terminated the handshake", null)
            },
            new Object[] {
                new HttpClientException("connect timed out", null),
                BankServiceError.BANK_SIDE_FAILURE.exception()
            },
            new Object[] {
                new HttpClientException("connection reset", null),
                BankServiceError.BANK_SIDE_FAILURE.exception()
            },
        };
    }

    private <T> void assertResultEqualsExpected(T result, T expected, String idField) {
        assertThat(result)
                .usingRecursiveComparison()
                // id field is randomly generated UUID
                .ignoringFields(idField)
                .isEqualTo(expected);
    }

    private FetchAccountsResponse getExpectedAccountsResponse(String holderName) {
        return new FetchAccountsResponse(
                Collections.singletonList(
                        TransactionalAccount.nxBuilder()
                                .withType(TransactionalAccountType.CHECKING)
                                .withPaymentAccountFlag()
                                .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(789.01)))
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier("BE39000000076000")
                                                .withAccountNumber("BE39000000076000")
                                                .withAccountName("DUMMY_NAME")
                                                .addIdentifier(
                                                        new IbanIdentifier("BE39000000076000"))
                                                .build())
                                .addHolderName(holderName)
                                .setApiIdentifier(TEST_LOGICAL_ID)
                                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                                .build()
                                .orElseThrow(IllegalStateException::new)
                                .toSystemAccount(user, provider)));
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
                        .setAmount(ExactCurrencyAmount.of(12.25, CURRENCY))
                        .setDate(new Date(1596067200000L))
                        .build()
                        .toSystemTransaction(false),
                Transaction.builder()
                        .setDescription("SEPA CREDIT TRANSFER to somewhere")
                        .setAmount(ExactCurrencyAmount.of(-10, CURRENCY))
                        .setDate(new Date(1596153600000L))
                        .build()
                        .toSystemTransaction(false));
    }

    private List<se.tink.backend.aggregation.agents.models.Transaction> getLastPageTransactions() {
        return Collections.singletonList(
                Transaction.builder()
                        .setDescription("SEPA CREDIT TRANSFER1")
                        .setAmount(ExactCurrencyAmount.of(99.99, CURRENCY))
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
