package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngApiInputData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngUserAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters.IngBaseTinkClientConfigurator;
import se.tink.backend.aggregation.eidassigner.FakeQsealcSigner;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;

@RunWith(JUnitParamsRunner.class)
public final class IngBaseTransactionalAccountRefreshControllerTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase/fetcher/resources/";
    private static final String NOT_FOUND_ERROR_CODE = "NOT_FOUND";
    private static final String RANDOM_ERROR_CODE = "SOME_RANDOM_ERROR_CODE";
    private static final String CONNECTION_RESET_MESSAGE = "connection reset";
    private static final String CONNECTION_TIMEOUT_MESSAGE = "connect timed out";
    private static final String READ_TIMEOUT_MESSAGE = "read timed out";
    private static final String FAILED_TO_RESPOND_MESSAGE = "failed to respond";
    private static final boolean USER_IS_AUTHENTICATED = true;
    private static final boolean USER_HAS_CLIENT_ID = true;
    private static final AgentError BANK_SIDE_FAILURE = BankServiceError.BANK_SIDE_FAILURE;

    private TransactionalAccountRefreshController refreshController;
    private TinkHttpClient tinkHttpClient;
    private PersistentStorage persistentStorage;
    private Provider provider;

    @Mock private HttpResponse response;
    @Mock private MetricRefreshController metricRefreshController;
    @Mock private Filter executionFilter;
    @Mock private ErrorResponse errorResponse;

    @Before
    public void prepareBasicTestConfiguration() {
        MockitoAnnotations.openMocks(this);
        tinkHttpClient = createTinkHttpClient();
        tinkHttpClient.addFilter(executionFilter);
        setUpPersistentStorage(USER_IS_AUTHENTICATED, USER_HAS_CLIENT_ID);
        IngBaseApiClient ingBaseApiClient = createIngBaseApiClient();
        setUpProvider();
        createConfiguredRefreshController(ingBaseApiClient);
        given(metricRefreshController.buildAction(Mockito.any(MetricId.class), Mockito.anyList()))
                .willReturn(Mockito.mock(MetricRefreshAction.class));
        given(executionFilter.handle(any())).willReturn(response);
    }

    @Test
    @Parameters(method = "prepareBankServiceErrorData")
    public void shouldThrowBankServiceExceptionWhenBankRespondsWithProperStatus(
            int statusCode, String errorCode, AgentError agentError) {

        // given
        bankRespondsWith(statusCode, errorCode);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .hasFieldOrPropertyWithValue("error", agentError);
    }

    @SuppressWarnings("unused")
    private Object[] prepareBankServiceErrorData() {
        AgentError noBankService = BankServiceError.NO_BANK_SERVICE;

        return new Object[][] {
            {500, NOT_FOUND_ERROR_CODE, BANK_SIDE_FAILURE},
            {501, NOT_FOUND_ERROR_CODE, BANK_SIDE_FAILURE},
            {502, RANDOM_ERROR_CODE, noBankService},
            {503, NOT_FOUND_ERROR_CODE, noBankService},
            {504, RANDOM_ERROR_CODE, noBankService},
            {507, NOT_FOUND_ERROR_CODE, BANK_SIDE_FAILURE}
        };
    }

    @Test
    @Parameters(method = "prepareHttpErrorData")
    public void shouldThrowHttpExceptionWhenBankRespondsWithProperStatus(
            int statusCode, String errorCode) {

        // given
        bankRespondsWith(statusCode, errorCode);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .isExactlyInstanceOf(HttpResponseException.class);
    }

    @SuppressWarnings("unused")
    private Object[] prepareHttpErrorData() {
        return new Object[][] {
            {501, RANDOM_ERROR_CODE},
            {505, RANDOM_ERROR_CODE},
            {507, RANDOM_ERROR_CODE}
        };
    }

    @Test
    @Parameters(method = "prepareMissingUserData")
    public void shouldNotRefreshWhenMissingKeyUserDataInResponse(
            boolean userIsAuthenticated, boolean userHasClientId, String expectedExceptionMessage) {

        // given
        setUpTestDataWithMissingKeyUserData(userIsAuthenticated, userHasClientId);

        // and
        bankRespondsWith(200);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining(expectedExceptionMessage);
    }

    @SuppressWarnings("unused")
    private Object[] prepareMissingUserData() {
        return new Object[][] {
            {!USER_IS_AUTHENTICATED, USER_HAS_CLIENT_ID, "Cannot find Token!"},
            {USER_IS_AUTHENTICATED, !USER_HAS_CLIENT_ID, "Cannot find client id!"}
        };
    }

    @Test
    @Parameters(method = "prepareUnauthorizedErrorData")
    public void shouldThrowProperExceptionWhenBankReturnsUnauthorizedStatus(
            String errorBody, String errorCode, AgentError agentError) {

        // given
        bankRespondsWith(401, errorCode);

        // and
        given(response.getBody(String.class)).willReturn(errorBody);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .hasFieldOrPropertyWithValue("error", agentError);
    }

    @SuppressWarnings("unused")
    private Object[] prepareUnauthorizedErrorData() {
        String signatureInvalid = "Signature invalid";
        String signatureUnverified = "Signature could not be successfully verified";

        return new Object[][] {
            {signatureUnverified, RANDOM_ERROR_CODE, BANK_SIDE_FAILURE},
            {
                signatureInvalid, RANDOM_ERROR_CODE, SessionError.SESSION_EXPIRED,
            },
            {signatureInvalid, NOT_FOUND_ERROR_CODE, BANK_SIDE_FAILURE},
            {signatureUnverified, NOT_FOUND_ERROR_CODE, BANK_SIDE_FAILURE}
        };
    }

    @Test
    @Parameters(method = "prepareRetryErrorData")
    public void shouldRetrySuccessfullyOnHttpClientExceptionWith(String exceptionMessage) {

        // given
        given(response.getBody(FetchAccountsResponse.class)).willReturn(accountsResponse());
        given(response.getBody(FetchBalancesResponse.class)).willReturn(balancesResponse());

        // and
        bankRespondsCorrectlyWithAccountsAfterSecondRequest(exceptionMessage);

        // expect
        assertThatNoException().isThrownBy(() -> refreshController.fetchCheckingAccounts());
    }

    @SuppressWarnings("unused")
    private Object[] prepareRetryErrorData() {
        return new Object[] {
            "Remote host terminated the handshake",
            CONNECTION_RESET_MESSAGE,
            CONNECTION_TIMEOUT_MESSAGE,
            READ_TIMEOUT_MESSAGE,
            FAILED_TO_RESPOND_MESSAGE
        };
    }

    @Test
    @Parameters(method = "prepareTimeoutErrorData")
    public void shouldThrowBankSideErrorWhenTimeout(
            String exceptionMessage, AgentError agentError) {

        // given
        given(executionFilter.handle(any()))
                .willThrow(new HttpClientException(exceptionMessage, null));

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .hasFieldOrPropertyWithValue("error", agentError);
    }

    @SuppressWarnings("unused")
    private Object[] prepareTimeoutErrorData() {
        return new Object[][] {
            {CONNECTION_RESET_MESSAGE, BANK_SIDE_FAILURE},
            {CONNECTION_TIMEOUT_MESSAGE, BANK_SIDE_FAILURE},
            {READ_TIMEOUT_MESSAGE, BANK_SIDE_FAILURE},
            {FAILED_TO_RESPOND_MESSAGE, BANK_SIDE_FAILURE}
        };
    }

    private void createConfiguredRefreshController(IngBaseApiClient ingBaseApiClient) {
        AccountFetcher<TransactionalAccount> accountFetcher =
                new IngBaseAccountsFetcher(
                        ingBaseApiClient, "EUR", mock(MarketConfiguration.class));

        refreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        new UpdateController(provider, new User()),
                        accountFetcher,
                        mock(TransactionFetcherController.class));
    }

    private IngBaseApiClient createIngBaseApiClient() {
        Credentials credentials = getFileContent("ing_credentials.json", Credentials.class);
        AgentComponentProvider agentComponentProvider = mock(AgentComponentProvider.class);
        given(agentComponentProvider.getRandomValueGenerator())
                .willReturn(new MockRandomValueGenerator());
        given(agentComponentProvider.getLocalDateTimeSource())
                .willReturn(new ConstantLocalDateTimeSource());

        return new IngBaseApiClient(
                tinkHttpClient,
                persistentStorage,
                new ProviderSessionCacheController(new AgentTestContext(credentials)),
                mock(MarketConfiguration.class),
                new FakeQsealcSigner(),
                createIngApiInputDataMock(),
                agentComponentProvider);
    }

    private TinkHttpClient createTinkHttpClient() {
        TinkHttpClient client =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
        new IngBaseTinkClientConfigurator().configureClient(client, 1, 1);

        return client;
    }

    @SneakyThrows
    private static <T> T getFileContent(String fileName, Class<T> className) {
        String fileData =
                new String(
                        Files.readAllBytes(Paths.get(RESOURCES_PATH).resolve(fileName)),
                        StandardCharsets.UTF_8);

        return SerializationUtils.deserializeFromString(fileData, className);
    }

    private IngApiInputData createIngApiInputDataMock() {
        return IngApiInputData.builder()
                .userAuthenticationData(mock(IngUserAuthenticationData.class))
                .strongAuthenticationState(mock(StrongAuthenticationState.class))
                .credentialsRequest(mock(CredentialsRequest.class))
                .build();
    }

    private void setUpPersistentStorage(boolean userIsAuthenticated, boolean userHasClientId) {
        persistentStorage = new PersistentStorage();

        if (userIsAuthenticated) {
            OAuth2Token token =
                    OAuth2Token.create("bearer", "ing-access-token", "ing-refresh-token", 12345L);
            persistentStorage.put(StorageKeys.TOKEN, token);
        }
        if (userHasClientId) {
            persistentStorage.put(StorageKeys.CLIENT_ID, "client-id");
        }
    }

    private void setUpProvider() {
        provider = new Provider();
        provider.setMarket("BE");
        provider.setCurrency("EUR");
    }

    private void setUpTestDataWithMissingKeyUserData(
            boolean isUserAuthenticated, boolean hasUserClientId) {
        setUpPersistentStorage(isUserAuthenticated, hasUserClientId);
        IngBaseApiClient ingBaseApiClient = createIngBaseApiClient();
        createConfiguredRefreshController(ingBaseApiClient);
    }

    private void bankRespondsWith(int statusCode) {
        given(response.getStatus()).willReturn(statusCode);
    }

    private void bankRespondsWith(int statusCode, String errorCode) {
        bankRespondsWith(statusCode);
        setUpErrorResponse(errorCode);
    }

    private void bankRespondsCorrectlyWithAccountsAfterSecondRequest(String exceptionMessage) {
        given(executionFilter.handle(any()))
                .willThrow(new HttpClientException(exceptionMessage, null))
                .willReturn(response);
    }

    private void setUpErrorResponse(String errorCode) {
        given(response.getBody(ErrorResponse.class)).willReturn(errorResponse);
        given(errorResponse.getErrorCode()).willReturn(errorCode);
    }

    private FetchAccountsResponse accountsResponse() {
        return getFileContent("accounts_response.json", FetchAccountsResponse.class);
    }

    private FetchBalancesResponse balancesResponse() {
        return getFileContent("balances_response.json", FetchBalancesResponse.class);
    }
}
