package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.refresh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
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
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngApiInputData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseTinkClientConfigurator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngUserAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.IngBaseAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchBalancesResponse;
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
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;

@RunWith(JUnitParamsRunner.class)
public final class IngBaseTransactionalAccountRefreshControllerTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase/refresh/resources/";
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String RANDOM_CODE = "SOME_RANDOM_CODE";
    private static final String TERMINATED_HANDSHAKE_EXCEPTION_MESSAGE =
            "Remote host terminated the handshake";
    private static final FetchAccountsResponse FETCH_ACCOUNTS_RESPONSE =
            getFileContent("accounts_response.json", FetchAccountsResponse.class);
    private static final FetchAccountsResponse FETCH_ACCOUNTS_NO_OWNER_RESPONSE =
            getFileContent("accounts_response_no_owner.json", FetchAccountsResponse.class);
    private static final FetchBalancesResponse FETCH_BALANCES_RESPONSE =
            getFileContent("balances_response.json", FetchBalancesResponse.class);
    private static final boolean USER_IS_AUTHENTICATED = true;
    private static final boolean USER_HAS_CLIENT_ID = true;
    private static final int MAX_ATTEMPTS = 1;
    private static final int RETRY_SLEEP_MILLISECONDS = 1;

    private TransactionalAccountRefreshController refreshController;
    private TinkHttpClient tinkHttpClient;
    private PersistentStorage persistentStorage;
    private Provider provider;
    private final User user = new User();
    private final BankServiceException bankSideFailureException =
            BankServiceError.BANK_SIDE_FAILURE.exception();

    @Mock private HttpResponse response;
    @Mock private MetricRefreshController metricRefreshController;
    @Mock private Filter executionFilter;
    @Mock private TransactionFetcherController<TransactionalAccount> transactionFetcherController;
    @Mock private AgentComponentProvider agentComponentProvider;
    @Mock private ErrorResponse errorResponse;

    @Test
    @Parameters(method = "prepareErrorStatusesAndErrorCodesAndErrorTypes")
    public void shouldThrowProperExceptionWhenBankRespondsWith5xxStatus(
            int statusCode,
            String errorCode,
            Object expectedException,
            String expectedExceptionMessage) {

        // given
        bankRespondsWith(statusCode);

        // and
        setUpErrorResponse(errorCode);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .isExactlyInstanceOf(expectedException.getClass())
                .hasMessageContaining(expectedExceptionMessage);
    }

    @SuppressWarnings("unused")
    private Object[] prepareErrorStatusesAndErrorCodesAndErrorTypes() {
        HttpResponseException httpException = new HttpResponseException(null, null);
        BankServiceException noBankServiceException = BankServiceError.NO_BANK_SERVICE.exception();
        String noBankServiceExceptionMessage = "Cause: BankServiceError.NO_BANK_SERVICE";
        String issueAtIngExceptionMessage = "Issue at ING, contact developer support";

        return new Object[][] {
            {500, NOT_FOUND, bankSideFailureException, "Http status: 500"},
            {500, RANDOM_CODE, bankSideFailureException, "Http status: 500"},
            {501, NOT_FOUND, bankSideFailureException, issueAtIngExceptionMessage},
            {501, RANDOM_CODE, httpException, "Response statusCode: 501"},
            {502, NOT_FOUND, noBankServiceException, noBankServiceExceptionMessage},
            {502, RANDOM_CODE, noBankServiceException, noBankServiceExceptionMessage},
            {503, NOT_FOUND, noBankServiceException, "Http status: 503"},
            {503, RANDOM_CODE, noBankServiceException, "Http status: 503"},
            {504, NOT_FOUND, noBankServiceException, noBankServiceExceptionMessage},
            {504, RANDOM_CODE, noBankServiceException, noBankServiceExceptionMessage},
            {505, NOT_FOUND, bankSideFailureException, issueAtIngExceptionMessage},
            {505, RANDOM_CODE, httpException, "Response statusCode: 505"},
            {507, NOT_FOUND, bankSideFailureException, issueAtIngExceptionMessage},
            {507, RANDOM_CODE, httpException, "Response statusCode: 507"}
        };
    }

    @Test
    @Parameters(method = "prepareMissingUserData")
    public void shouldNotRefreshWhenMissingKeyUserDataInResponse(
            boolean isUserAuthenticated, boolean hasUserClientId, String expectedExceptionMessage) {

        // given
        setUpTestDataWithMissingKeyUserData(isUserAuthenticated, hasUserClientId);

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
            String errorBody,
            String errorCode,
            Object expectedException,
            String expectedExceptionMessage) {

        // given
        setUpErrorResponse(errorCode);

        // and
        bankRespondsWith(401);

        // and
        given(response.getBody(String.class)).willReturn(errorBody);

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .isExactlyInstanceOf(expectedException.getClass())
                .hasMessageContaining(expectedExceptionMessage);
    }

    @SuppressWarnings("unused")
    private Object[] prepareUnauthorizedErrorData() {
        String signatureError = "Signature verify error";
        String signatureInvalid = "Signature invalid";
        String signatureUnverified = "Signature could not be successfully verified";

        return new Object[][] {
            {signatureUnverified, RANDOM_CODE, bankSideFailureException, signatureError},
            {
                signatureInvalid,
                RANDOM_CODE,
                SessionError.SESSION_EXPIRED.exception(),
                "Cause: SessionError.SESSION_EXPIRED"
            },
            {signatureInvalid, NOT_FOUND, bankSideFailureException, signatureInvalid},
            {signatureUnverified, NOT_FOUND, bankSideFailureException, signatureError},
        };
    }

    @Test
    public void shouldThrowAndRetrySuccessfullyOnTerminatedHandshake() {

        // given
        given(response.getBody(FetchAccountsResponse.class)).willReturn(FETCH_ACCOUNTS_RESPONSE);
        given(response.getBody(FetchBalancesResponse.class)).willReturn(FETCH_BALANCES_RESPONSE);

        // and
        given(executionFilter.handle(any()))
                .willThrow(new HttpClientException(TERMINATED_HANDSHAKE_EXCEPTION_MESSAGE, null))
                .willReturn(response);

        // when
        se.tink.backend.aggregation.agents.FetchAccountsResponse result =
                refreshController.fetchCheckingAccounts();

        // expect
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("accounts.id")
                .isEqualTo(getExpectedAccountsResponse());
    }

    @Test
    @Parameters(method = "prepareTimeoutErrorData")
    public void shouldThrowBankSideErrorWhenTimeout(
            String exceptionMessage, Object expectedException, String expectedExceptionMessage) {

        // given
        given(executionFilter.handle(any()))
                .willThrow(new HttpClientException(exceptionMessage, null));

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .isExactlyInstanceOf(expectedException.getClass())
                .hasMessageContaining(expectedExceptionMessage);
    }

    @SuppressWarnings("unused")
    private Object[] prepareTimeoutErrorData() {
        String bankSideFailureExceptionMessage = "Cause: BankServiceError.BANK_SIDE_FAILURE";
        String randomMessage = "some random test exception message";

        return new Object[][] {
            {"connection reset", bankSideFailureException, bankSideFailureExceptionMessage},
            {"connect timed out", bankSideFailureException, bankSideFailureExceptionMessage},
            {"read timed out", bankSideFailureException, bankSideFailureExceptionMessage},
            {"failed to respond", bankSideFailureException, bankSideFailureExceptionMessage},
            {randomMessage, new HttpClientException(randomMessage, null), randomMessage}
        };
    }

    @Test
    public void shouldReturnCheckingAccountWhenAccountOwnerIsMissing() {

        // given
        bankRespondsWith(200);

        // and
        given(response.getBody(FetchAccountsResponse.class))
                .willReturn(FETCH_ACCOUNTS_NO_OWNER_RESPONSE);
        given(response.getBody(FetchBalancesResponse.class)).willReturn(FETCH_BALANCES_RESPONSE);

        // when
        se.tink.backend.aggregation.agents.FetchAccountsResponse result =
                refreshController.fetchCheckingAccounts();

        // expect
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("accounts.id")
                .isEqualTo(getExpectedAccountsResponse());
    }

    @Test
    @Parameters(method = "provideResponsesWithIncompleteData")
    public void shouldNotReturnCheckingAccountWhenKeyAccountDataMissingInResponse(String fileName) {

        // given
        bankRespondsWith(200);

        // and
        given(response.getBody(FetchAccountsResponse.class))
                .willReturn(getFileContent(fileName, FetchAccountsResponse.class));

        // expect
        assertThatThrownBy(() -> refreshController.fetchCheckingAccounts())
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @SuppressWarnings("unused")
    private String[] provideResponsesWithIncompleteData() {
        return new String[] {
            "accounts_response_no_account_name.json", "accounts_response_no_iban.json"
        };
    }

    @Before
    public void prepareTestBasicTestConfiguration() {
        MockitoAnnotations.openMocks(this);

        given(agentComponentProvider.getRandomValueGenerator())
                .willReturn(new MockRandomValueGenerator());
        given(agentComponentProvider.getLocalDateTimeSource())
                .willReturn(new ConstantLocalDateTimeSource());

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

    private void createConfiguredRefreshController(IngBaseApiClient ingBaseApiClient) {
        AccountFetcher<TransactionalAccount> accountFetcher =
                new IngBaseAccountsFetcher(
                        ingBaseApiClient, "EUR", mock(MarketConfiguration.class));

        refreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        new UpdateController(provider, user),
                        accountFetcher,
                        transactionFetcherController);
    }

    private se.tink.backend.aggregation.agents.FetchAccountsResponse getExpectedAccountsResponse() {
        double balanceAmount = 0.05;

        return new se.tink.backend.aggregation.agents.FetchAccountsResponse(
                Collections.singletonList(
                        TransactionalAccount.nxBuilder()
                                .withType(TransactionalAccountType.CHECKING)
                                .withPaymentAccountFlag()
                                .withBalance(
                                        BalanceModule.builder()
                                                .withBalance(
                                                        ExactCurrencyAmount.inEUR(balanceAmount))
                                                .setAvailableBalance(
                                                        ExactCurrencyAmount.inEUR(balanceAmount))
                                                .build())
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier("BE54000000000000")
                                                .withAccountNumber("BE54000000000000")
                                                .withAccountName("ING Lion Account")
                                                .addIdentifier(
                                                        new IbanIdentifier("BE54000000000000"))
                                                .build())
                                .addHolderName(null)
                                .setApiIdentifier("resource_id")
                                .build()
                                .orElseThrow(IllegalStateException::new)
                                .toSystemAccount(user, provider)));
    }

    private void setUpErrorResponse(String errorCode) {
        given(response.getBody(ErrorResponse.class)).willReturn(errorResponse);
        given(errorResponse.getErrorCode()).willReturn(errorCode);
    }

    private IngBaseApiClient createIngBaseApiClient() {
        Credentials credentials = getFileContent("ing_credentials.json", Credentials.class);

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
        new IngBaseTinkClientConfigurator()
                .configureClient(client, MAX_ATTEMPTS, RETRY_SLEEP_MILLISECONDS);

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

    private void setUpPersistentStorage(boolean isUserAuthenticated, boolean hasUserClientId) {
        persistentStorage = new PersistentStorage();

        if (isUserAuthenticated) {
            OAuth2Token token =
                    OAuth2Token.create("bearer", "ing-access-token", "ing-refresh-token", 12345L);
            persistentStorage.put(StorageKeys.TOKEN, token);
        }
        if (hasUserClientId) {
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

        setUpProvider();

        createConfiguredRefreshController(ingBaseApiClient);
    }

    private void bankRespondsWith(int statusCode) {
        given(response.getStatus()).willReturn(statusCode);
    }
}
