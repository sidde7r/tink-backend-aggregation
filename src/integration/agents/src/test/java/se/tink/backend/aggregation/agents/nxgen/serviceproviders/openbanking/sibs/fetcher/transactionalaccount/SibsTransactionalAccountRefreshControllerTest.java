package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Objects;
import javax.net.ssl.SSLHandshakeException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsRateLimitFilterProperties;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsRetryFilterProperties;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsTinkApiClientConfigurator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.Consent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.TransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.to_system.AccountConverter;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;

@RunWith(JUnitParamsRunner.class)
public class SibsTransactionalAccountRefreshControllerTest {

    private static final String EMPTY_RESPONSE_BODY = "";
    private static final int TEST_RETRY_SLEEP_MS = 1;
    private static final int TEST_MAX_RETRIES_NUMBER = 1;

    @Mock private MetricRefreshController metricRefreshController;
    @Mock private Filter callFilter;
    @Mock private HttpResponse response;

    private final PersistentStorage persistentStorage = new PersistentStorage();
    private Provider provider;
    private TransactionalAccountRefreshController refreshController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SibsUserState userState = getUserStateWithConsent();
        provider = provider();
        SibsBaseApiClient apiClient = getConfiguredSibsApiClient(userState);
        SibsTransactionalAccountFetcher accountFetcher =
                new SibsTransactionalAccountFetcher(apiClient);
        SibsTransactionalAccountTransactionFetcher transactionFetcher =
                new SibsTransactionalAccountTransactionFetcher(
                        apiClient,
                        mock(CredentialsRequest.class),
                        userState,
                        new ConstantLocalDateTimeSource());

        refreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        new UpdateController(provider, new User()),
                        accountFetcher,
                        new TransactionFetcherController<>(
                                mock(TransactionPaginationHelper.class),
                                new TransactionKeyPaginationController<>(transactionFetcher)));

        defaultMockConfiguration();
    }

    @Test
    @Parameters
    public void shouldThrowProperErrorOnGivenErrorResponseStatus(
            int statusCode, AgentError agentError, String errorResponseBody) {
        // given
        bankRespondedWithGivenError(statusCode, errorResponseBody);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .hasFieldOrPropertyWithValue("error", agentError);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldThrowProperErrorOnGivenErrorResponseStatus() {
        return new Object[][] {
            {400, BankServiceError.BANK_SIDE_FAILURE, "BAD_REQUEST"},
            {401, SessionError.CONSENT_INVALID, EMPTY_RESPONSE_BODY},
            {429, BankServiceError.ACCESS_EXCEEDED, EMPTY_RESPONSE_BODY},
            {405, BankServiceError.BANK_SIDE_FAILURE, EMPTY_RESPONSE_BODY},
            {500, BankServiceError.BANK_SIDE_FAILURE, EMPTY_RESPONSE_BODY},
            {503, BankServiceError.NO_BANK_SERVICE, EMPTY_RESPONSE_BODY}
        };
    }

    @Test
    public void shouldRetryOnSslException() {
        // given
        bankReturnsCorrectResponseAfterSecondRequest();

        // when
        FetchAccountsResponse result = refreshController.fetchCheckingAccounts();

        // then
        assertEqualsIgnoringIds(result, getExpectedAccountsResponse());
    }

    @Test
    @Parameters({"connection reset", "connect timed out", "read timed out", "failed to respond"})
    public void shouldThrowExceptionOnConnectionTimeout(String errorMessage) {
        // given
        thereIsTimeoutExceptionOnRequestToTheBank(errorMessage);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .hasFieldOrPropertyWithValue("error", BankServiceError.BANK_SIDE_FAILURE);
    }

    private void thereIsTimeoutExceptionOnRequestToTheBank(String errorMessage) {
        given(callFilter.handle(any()))
                .willThrow(new HttpClientException(errorMessage, null))
                .willReturn(response);
    }

    private void bankReturnsCorrectResponseAfterSecondRequest() {
        given(callFilter.handle(any()))
                .willThrow(
                        new HttpClientException(
                                "ssl exception", new SSLHandshakeException(null), null))
                .willReturn(response);
    }

    private void bankRespondedWithGivenError(int statusCode, String errorResponseBody) {
        given(response.getStatus()).willReturn(statusCode);
        given(response.getBody(String.class)).willReturn(errorResponseBody);
        given(response.hasBody()).willReturn(true);
    }

    private void defaultMockConfiguration() {
        given(metricRefreshController.buildAction(any(), any()))
                .willReturn(mock(MetricRefreshAction.class));
        given(callFilter.handle(any())).willReturn(response);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(AccountsResponse.class))
                .willReturn(deserializeFromFile("accounts_response.json", AccountsResponse.class));
        given(response.getBody(BalancesResponse.class))
                .willReturn(deserializeFromFile("balance_response.json", BalancesResponse.class));
        given(response.getBody(TransactionsResponse.class))
                .willReturn(
                        deserializeFromFile(
                                "transactions_response.json", TransactionsResponse.class));
    }

    private AgentConfiguration<SibsConfiguration> agentConfiguration() {
        SibsConfiguration sibsConfiguration =
                deserializeFromFile("sibs_configuration.json", SibsConfiguration.class);

        return new AgentConfiguration.Builder<SibsConfiguration>()
                .setProviderSpecificConfiguration(sibsConfiguration)
                .setRedirectUrl("https://api.tink.test")
                .build();
    }

    private Provider provider() {
        Provider provider = new Provider();
        provider.setCurrency("EUR");
        provider.setMarket("BE");
        return provider;
    }

    private NextGenTinkHttpClient tinkhttpClient() {
        return NextGenTinkHttpClient.builder(
                        new FakeLogMasker(),
                        LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                .build();
    }

    private SibsBaseApiClient getConfiguredSibsApiClient(SibsUserState userState) {
        TinkHttpClient client = tinkhttpClient();
        AgentConfiguration<SibsConfiguration> agentConfiguration = agentConfiguration();
        new SibsTinkApiClientConfigurator()
                .applyFilters(
                        client,
                        new SibsRetryFilterProperties(
                                TEST_MAX_RETRIES_NUMBER,
                                TEST_RETRY_SLEEP_MS,
                                TEST_MAX_RETRIES_NUMBER),
                        new SibsRateLimitFilterProperties(
                                TEST_RETRY_SLEEP_MS, TEST_RETRY_SLEEP_MS, TEST_MAX_RETRIES_NUMBER),
                        "providerName");
        client.addFilter(callFilter);

        return new SibsBaseApiClient(
                client,
                userState,
                "xx",
                true,
                "127.0.0.1",
                new ConstantLocalDateTimeSource(),
                agentConfiguration);
    }

    private SibsUserState getUserStateWithConsent() {
        Consent consent =
                new Consent(
                        "DUMMY_CONSENT_ID",
                        new ConstantLocalDateTimeSource().now(ZoneOffset.UTC).toString());
        persistentStorage.put("CONSENT_ID", consent);

        return new SibsUserState(persistentStorage);
    }

    private FetchAccountsResponse getExpectedAccountsResponse() {
        String accountNumber = "PT62003506517194674519162";
        double accountBalance = 54273.9;
        TransactionalAccount transactionalAccount =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.CHECKING)
                        .withPaymentAccountFlag()
                        .withBalance(
                                BalanceModule.builder()
                                        .withBalance(ExactCurrencyAmount.inEUR(accountBalance))
                                        .setAvailableBalance(
                                                ExactCurrencyAmount.inEUR(accountBalance))
                                        .build())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountNumber)
                                        .withAccountNumber(accountNumber)
                                        .withAccountName(Objects.toString("USER_NAME", ""))
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.IBAN, accountNumber))
                                        .build())
                        .setApiIdentifier("DUMMY_RESOURCE")
                        .build()
                        .orElseThrow(IllegalStateException::new);

        return new FetchAccountsResponse(
                Collections.singletonList(
                        AccountConverter.toSystemAccount(
                                new User(), provider, transactionalAccount)));
    }

    private <T> void assertEqualsIgnoringIds(T result, T expected) {
        assertThat(result)
                .usingRecursiveComparison()
                // id field is randomly generated UUID
                .ignoringFields("accounts.id", "transactions.id")
                .isEqualTo(expected);
    }

    private static <T> T deserializeFromFile(String fileName, Class<T> clazz) {
        String resourcePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs/resources/";

        return SerializationUtils.deserializeFromString(
                Paths.get(resourcePath + fileName).toFile(), clazz);
    }
}
