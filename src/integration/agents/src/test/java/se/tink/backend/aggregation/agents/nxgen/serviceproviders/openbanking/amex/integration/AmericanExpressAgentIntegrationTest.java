package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.ACCESS_TOKEN_2;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.ACCOUNT_NUMBER_1;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.ACCOUNT_NUMBER_2;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.AUTH_CODE_1;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.AUTH_CODE_2;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.EXPIRED_REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.VALID_REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createAccessTokenResponseJsonString;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createAccessTokenRevokedErrorResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createAccountsResponseJsonString;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createBalancesResponseJsonString;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createMultiTokenWithExpiredAccessToken;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createMultiTokenWithValidAccessToken;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createRefreshAccessTokenRequestBody;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createRetrieveAccessTokenRequestBody;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createStatementPeriodsResponseJsonString;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.createTransactionsResponseJsonString;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.getAuthorizeUrl;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestValidators.createAuthHeaderMatchingPattern;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestValidators.getAmexRequestIdMatchingRegex;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestValidators.getSupplementalKeyMatchingRegex;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants.Urls;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac.HmacMultiTokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps.ThirdPartyAppAuthenticationStepCreator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacMultiToken;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AmericanExpressAgentIntegrationTest extends IntegrationTestBase {

    @Test
    public void testAuthenticationForTheFirstTimeLogin() throws Exception {
        // given
        final CredentialsRequest credentialsRequest = createCredentialsRequest();
        final Credentials credentials = credentialsRequest.getCredentials();
        final AgentComponentProvider agentComponentProvider =
                createAgentComponentProvider(credentialsRequest);
        final AmericanExpressAgent americanExpressAgent =
                new AmericanExpressAgent(agentComponentProvider);

        final String expectedAccessToken1 = UUID.randomUUID().toString();
        final String expectedAccessToken2 = UUID.randomUUID().toString();

        recordRetrieveAccessTokenResponse(expectedAccessToken1, expectedAccessToken2);

        // when
        // 1. Check there is no valid access token and assert Third Party App Call will be next
        // authentication step
        final SteppableAuthenticationResponse intermediateAuthResponse =
                makeInitialLoginCall(americanExpressAgent, credentials);
        verifyThirdPartyAppCallStepFollowsInitialStep(intermediateAuthResponse);

        // 2. Make a call to the bank to retrieve access token based on the callback data from third
        // party app
        final SteppableAuthenticationResponse finalAuthResponse =
                makeAccessTokenRetrievalAfterThirdPartyAppCallback(
                        americanExpressAgent, credentials);
        verifyAccessTokenWasRetrieved(
                finalAuthResponse,
                americanExpressAgent.getHmacMultiTokenStorage(),
                expectedAccessToken1,
                expectedAccessToken2);
    }

    @Test
    public void testAuthenticationWhenRefreshTokenHadExpired() throws Exception {
        // given
        final CredentialsRequest credentialsRequest = createCredentialsRequest();
        final Credentials credentials = credentialsRequest.getCredentials();
        final AgentComponentProvider agentComponentProvider =
                createAgentComponentProvider(credentialsRequest);
        final AmericanExpressAgent americanExpressAgent =
                new AmericanExpressAgent(agentComponentProvider);

        final HmacMultiToken expiredHmacMultiToken = createMultiTokenWithExpiredAccessToken();
        americanExpressAgent.getHmacMultiTokenStorage().storeToken(expiredHmacMultiToken);

        final String expectedAccessToken1 = UUID.randomUUID().toString();
        final String expectedAccessToken2 = UUID.randomUUID().toString();

        recordRefreshTokenResponseForExpiredRefreshToken();
        recordRetrieveAccessTokenResponse(expectedAccessToken1, expectedAccessToken2);

        // when
        // 1. Get expired access token, try to refresh it and assert Third Party App Call will be
        // next authentication step
        final SteppableAuthenticationResponse intermediateAuthResponse =
                makeInitialLoginCall(americanExpressAgent, credentials);
        verifyRefreshTokenFollowedInitialStep();
        verifyThirdPartyAppCallIsTheNextAuthStep(intermediateAuthResponse);

        // 2. Make a call to the bank to retrieve access token based on the callback data from third
        // party app
        final SteppableAuthenticationResponse finalAuthResponse =
                makeAccessTokenRetrievalAfterThirdPartyAppCallback(
                        americanExpressAgent, credentials);
        verifyAccessTokenWasRetrieved(
                finalAuthResponse,
                americanExpressAgent.getHmacMultiTokenStorage(),
                expectedAccessToken1,
                expectedAccessToken2);
    }

    @Test
    public void testAccountsFetchingWithValidAccessToken() {
        // given
        final CredentialsRequest credentialsRequest = createCredentialsRequest();
        final AgentComponentProvider agentComponentProvider =
                createAgentComponentProvider(credentialsRequest);
        final AmericanExpressAgent americanExpressAgent =
                new AmericanExpressAgent(agentComponentProvider);

        final HmacMultiToken validHmacMultiToken = createMultiTokenWithValidAccessToken();
        americanExpressAgent.getHmacMultiTokenStorage().storeToken(validHmacMultiToken);

        final String accessToken1 = validHmacMultiToken.getTokens().get(0).getAccessToken();
        final String accessToken2 = validHmacMultiToken.getTokens().get(1).getAccessToken();
        recordFetchAccountsResponse(accessToken1, accessToken2);
        recordFetchBalancesResponse(accessToken1, accessToken2);
        recordFetchStatementPeriods(accessToken1, accessToken2);
        // when
        final FetchAccountsResponse fetchAccountsResponse =
                americanExpressAgent.fetchCreditCardAccounts();

        // then
        verifyFetchAccountsResponse(fetchAccountsResponse);
    }

    @Test
    public void testTransactionsFetchingWithValidAccessToken() {
        // given
        final CredentialsRequest credentialsRequest = createCredentialsRequest();
        final AgentComponentProvider agentComponentProvider =
                createAgentComponentProvider(credentialsRequest);
        final AmericanExpressAgent americanExpressAgent =
                new AmericanExpressAgent(agentComponentProvider);

        final HmacMultiToken validHmacMultiToken = createMultiTokenWithValidAccessToken();
        americanExpressAgent.getHmacMultiTokenStorage().storeToken(validHmacMultiToken);

        final String accessToken1 = validHmacMultiToken.getTokens().get(0).getAccessToken();
        final String accessToken2 = validHmacMultiToken.getTokens().get(1).getAccessToken();
        recordFetchAccountsResponse(accessToken1, accessToken2);
        recordFetchBalancesResponse(accessToken1, accessToken2);
        recordFetchStatementPeriods(accessToken1, accessToken2);
        recordFetchPendingTransactionsResponse(accessToken1, accessToken2);
        recordFetchTransactionsResponse(accessToken1, accessToken2);

        // when
        final FetchTransactionsResponse fetchTransactionsResponse =
                americanExpressAgent.fetchCreditCardTransactions();

        // then
        verifyFetchTransactionsResponse(fetchTransactionsResponse);
    }

    @Test
    public void testAccountsFetchingAfterUserRevokedTheAccessToken() {
        // given
        final CredentialsRequest credentialsRequest = createCredentialsRequest();
        final AgentComponentProvider agentComponentProvider =
                createAgentComponentProvider(credentialsRequest);
        final AmericanExpressAgent americanExpressAgent =
                new AmericanExpressAgent(agentComponentProvider);

        final HmacMultiToken validHmacMultiToken = createMultiTokenWithValidAccessToken();
        americanExpressAgent.getHmacMultiTokenStorage().storeToken(validHmacMultiToken);

        final String accessToken1 = validHmacMultiToken.getTokens().get(0).getAccessToken();
        final String accessToken2 = validHmacMultiToken.getTokens().get(1).getAccessToken();
        recordFetchAccountsResponseAfterAccessTokenHadBeenRevoked(accessToken1, accessToken2);

        // when
        final Throwable thrown = catchThrowable(americanExpressAgent::fetchCreditCardAccounts);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.CONSENT_REVOKED");
    }

    private SteppableAuthenticationResponse makeInitialLoginCall(
            AmericanExpressAgent americanExpressAgent, Credentials credentials) throws Exception {
        return americanExpressAgent.login(
                SteppableAuthenticationRequest.initialRequest(credentials));
    }

    private void verifyThirdPartyAppCallStepFollowsInitialStep(
            SteppableAuthenticationResponse intermediateAuthResponse) {
        verifyThirdPartyAppCallIsTheNextAuthStep(intermediateAuthResponse);

        wireMockRule.verify(
                0, postRequestedFor(urlPathEqualTo(Urls.REFRESH_TOKEN_PATH.toUri().getPath())));
    }

    private void verifyThirdPartyAppCallIsTheNextAuthStep(
            SteppableAuthenticationResponse intermediateAuthResponse) {
        assertThat(intermediateAuthResponse.getStepIdentifier().isPresent()).isTrue();
        assertThat(intermediateAuthResponse.getStepIdentifier().get())
                .isEqualTo(ThirdPartyAppAuthenticationStepCreator.STEP_NAME);
        assertThat(intermediateAuthResponse.getSupplementInformationRequester()).isNotNull();

        assertSupplementInformationRequesterContentIsValid(
                intermediateAuthResponse.getSupplementInformationRequester());
    }

    private SteppableAuthenticationResponse makeAccessTokenRetrievalAfterThirdPartyAppCallback(
            AmericanExpressAgent americanExpressAgent, Credentials credentials) throws Exception {
        // given
        final String callbackDataString = String.format("%s,%s", AUTH_CODE_1, AUTH_CODE_2);
        final AuthenticationRequest authenticationRequest = new AuthenticationRequest(credentials);

        final Map<String, String> callbackData = new HashMap<>();
        callbackData.put("authtoken", callbackDataString);
        authenticationRequest.withCallbackData(callbackData);

        // when
        return americanExpressAgent.login(
                SteppableAuthenticationRequest.subsequentRequest(
                        ThirdPartyAppAuthenticationStepCreator.STEP_NAME, authenticationRequest));
    }

    private void verifyAccessTokenWasRetrieved(
            SteppableAuthenticationResponse finalResponse,
            HmacMultiTokenStorage hmacMultiTokenStorage,
            String expectedAccessToken1,
            String expectedAccessToken2) {
        assertThat(finalResponse.getStepIdentifier().isPresent()).isFalse();
        assertThat(finalResponse.getSupplementInformationRequester()).isNull();

        wireMockRule.verify(
                postRequestedFor(urlPathEqualTo(Urls.RETRIEVE_TOKEN_PATH.toUri().getPath())));

        final Optional<HmacMultiToken> maybeHmacMultiToken = hmacMultiTokenStorage.getToken();
        assertThat(maybeHmacMultiToken.isPresent()).isTrue();

        final HmacMultiToken hmacMultiToken = maybeHmacMultiToken.get();
        assertThat(hmacMultiToken.getTokens()).hasSize(2);
        assertThat(hmacMultiToken.getTokens().get(0).getAccessToken())
                .isEqualTo(expectedAccessToken1);
        assertThat(hmacMultiToken.getTokens().get(1).getAccessToken())
                .isEqualTo(expectedAccessToken2);
    }

    private void verifyRefreshTokenFollowedInitialStep() {
        wireMockRule.verify(
                postRequestedFor(urlPathEqualTo(Urls.REFRESH_TOKEN_PATH.toUri().getPath())));
    }

    private void verifyFetchAccountsResponse(FetchAccountsResponse fetchAccountsResponse) {
        assertThat(fetchAccountsResponse.getAccounts()).hasSize(2);
    }

    private void verifyFetchTransactionsResponse(
            FetchTransactionsResponse fetchTransactionsResponse) {
        assertThat(fetchTransactionsResponse.getTransactions()).hasSize(2);
    }

    private void recordRetrieveAccessTokenResponse(String accessToken1, String accessToken2) {
        wireMockRule.stubFor(
                post(urlPathEqualTo(Urls.RETRIEVE_TOKEN_PATH.toUri().getPath()))
                        .withHeader(
                                AmericanExpressConstants.Headers.AUTHENTICATION,
                                matching(createAuthHeaderMatchingPattern(CLIENT_ID)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                HttpHeaders.CONTENT_TYPE,
                                equalTo(MediaType.APPLICATION_FORM_URLENCODED))
                        .withHeader(HttpHeaders.ACCEPT_ENCODING, equalTo("gzip,deflate"))
                        .withRequestBody(equalTo(createRetrieveAccessTokenRequestBody(AUTH_CODE_1)))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(
                                                createAccessTokenResponseJsonString(
                                                        accessToken1))));

        wireMockRule.stubFor(
                post(urlPathEqualTo(Urls.RETRIEVE_TOKEN_PATH.toUri().getPath()))
                        .withHeader(
                                AmericanExpressConstants.Headers.AUTHENTICATION,
                                matching(createAuthHeaderMatchingPattern(CLIENT_ID)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                HttpHeaders.CONTENT_TYPE,
                                equalTo(MediaType.APPLICATION_FORM_URLENCODED))
                        .withHeader(HttpHeaders.ACCEPT_ENCODING, equalTo("gzip,deflate"))
                        .withRequestBody(equalTo(createRetrieveAccessTokenRequestBody(AUTH_CODE_2)))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(
                                                createAccessTokenResponseJsonString(
                                                        accessToken2))));
    }

    private void recordRefreshTokenResponseForExpiredRefreshToken() {
        wireMockRule.stubFor(
                post(urlPathEqualTo(Urls.REFRESH_TOKEN_PATH.toUri().getPath()))
                        .withHeader(
                                AmericanExpressConstants.Headers.AUTHENTICATION,
                                matching(createAuthHeaderMatchingPattern(CLIENT_ID)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                HttpHeaders.CONTENT_TYPE,
                                equalTo(MediaType.APPLICATION_FORM_URLENCODED))
                        .withHeader(HttpHeaders.ACCEPT_ENCODING, equalTo("gzip,deflate"))
                        .withRequestBody(
                                equalTo(createRefreshAccessTokenRequestBody(VALID_REFRESH_TOKEN)))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(
                                                createAccessTokenResponseJsonString(
                                                        ACCESS_TOKEN_2))));

        wireMockRule.stubFor(
                post(urlPathEqualTo(Urls.REFRESH_TOKEN_PATH.toUri().getPath()))
                        .withHeader(
                                AmericanExpressConstants.Headers.AUTHENTICATION,
                                matching(createAuthHeaderMatchingPattern(CLIENT_ID)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                HttpHeaders.CONTENT_TYPE,
                                equalTo(MediaType.APPLICATION_FORM_URLENCODED))
                        .withHeader(HttpHeaders.ACCEPT_ENCODING, equalTo("gzip,deflate"))
                        .withRequestBody(
                                equalTo(createRefreshAccessTokenRequestBody(EXPIRED_REFRESH_TOKEN)))
                        .willReturn(
                                aResponse()
                                        .withStatus(401)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createAccessTokenRevokedErrorResponse())));
    }

    private void recordFetchAccountsResponse(String accessToken1, String accessToken2) {
        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_ACCOUNTS.toUri().getPath()))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken1)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(
                                                createAccountsResponseJsonString(
                                                        ACCOUNT_NUMBER_1))));

        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_ACCOUNTS.toUri().getPath()))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken2)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(
                                                createAccountsResponseJsonString(
                                                        ACCOUNT_NUMBER_2))));
    }

    private void recordFetchBalancesResponse(String accessToken1, String accessToken2) {
        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_BALANCES.toUri().getPath()))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken1)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createBalancesResponseJsonString())));

        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_BALANCES.toUri().getPath()))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken2)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createBalancesResponseJsonString())));
    }

    private void recordFetchStatementPeriods(String accessToken1, String accessToken2) {
        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_STATEMENT_PERIODS.toUri().getPath()))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken1)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createStatementPeriodsResponseJsonString())));

        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_STATEMENT_PERIODS.toUri().getPath()))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken2)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createStatementPeriodsResponseJsonString())));
    }

    private void recordFetchPendingTransactionsResponse(String accessToken1, String accessToken2) {
        final String expectedEndDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date());
        final String expectedStartDate = LocalDate.now().minusDays(30).toString().substring(0, 7);
        final String PENDING = "pending";
        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_TRANSACTIONS.toUri().getPath()))
                        .withQueryParam(
                                AmericanExpressConstants.QueryParams.QUERY_PARAM_START_DATE,
                                containing(expectedStartDate))
                        .withQueryParam(
                                AmericanExpressConstants.QueryParams.QUERY_PARAM_END_DATE,
                                containing(expectedEndDate))
                        .withQueryParam(
                                AmericanExpressConstants.QueryParams.STATUS, containing(PENDING))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken1)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createTransactionsResponseJsonString())));

        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_TRANSACTIONS.toUri().getPath()))
                        .withQueryParam(
                                AmericanExpressConstants.QueryParams.QUERY_PARAM_START_DATE,
                                containing(expectedStartDate))
                        .withQueryParam(
                                AmericanExpressConstants.QueryParams.QUERY_PARAM_END_DATE,
                                containing(expectedEndDate))
                        .withQueryParam(
                                AmericanExpressConstants.QueryParams.STATUS, containing(PENDING))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken2)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createTransactionsResponseJsonString())));
    }

    private void recordFetchTransactionsResponse(String accessToken1, String accessToken2) {
        final String expectedEndDate = "2021-03-02";
        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_TRANSACTIONS.toUri().getPath()))
                        .withQueryParam(
                                AmericanExpressConstants.QueryParams.QUERY_PARAM_STATEMENT_END_DATE,
                                containing(expectedEndDate))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken1)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createTransactionsResponseJsonString())));

        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_TRANSACTIONS.toUri().getPath()))
                        .withQueryParam(
                                AmericanExpressConstants.QueryParams.QUERY_PARAM_STATEMENT_END_DATE,
                                containing(expectedEndDate))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken2)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createTransactionsResponseJsonString())));
    }

    private void recordFetchAccountsResponseAfterAccessTokenHadBeenRevoked(
            String accessToken1, String accessToken2) {
        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_ACCOUNTS.toUri().getPath()))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken1)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(
                                                createAccountsResponseJsonString(
                                                        ACCOUNT_NUMBER_1))));

        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_ACCOUNTS.toUri().getPath()))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken2)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(401)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createAccessTokenRevokedErrorResponse())));

        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_STATEMENT_PERIODS.toUri().getPath()))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken1)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createStatementPeriodsResponseJsonString())));

        wireMockRule.stubFor(
                get(urlPathEqualTo(Urls.ENDPOINT_STATEMENT_PERIODS.toUri().getPath()))
                        .withHeader(
                                HttpHeaders.AUTHORIZATION,
                                matching(createAuthHeaderMatchingPattern(accessToken2)))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_API_KEY, equalTo(CLIENT_ID))
                        .withHeader(
                                AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                                matching(getAmexRequestIdMatchingRegex()))
                        .willReturn(
                                aResponse()
                                        .withStatus(401)
                                        .withHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .withBody(createAccessTokenRevokedErrorResponse())));
    }

    private static void assertSupplementInformationRequesterContentIsValid(
            SupplementInformationRequester supplementInformationRequester) {
        assertThat(supplementInformationRequester.getSupplementalWaitRequest().isPresent())
                .isTrue();
        final SupplementalWaitRequest supplementalWaitRequest =
                supplementInformationRequester.getSupplementalWaitRequest().get();
        assertThat(supplementalWaitRequest.getWaitFor()).isEqualTo(10L);
        assertThat(supplementalWaitRequest.getTimeUnit()).isEqualTo(TimeUnit.MINUTES);

        final String supplementalKeyMatchingRegex = getSupplementalKeyMatchingRegex();
        assertThat(supplementalWaitRequest.getKey())
                .matches(Pattern.compile(supplementalKeyMatchingRegex));

        final String state = supplementalWaitRequest.getKey().replace("tpcb_", "");
        final String expectedAuthorizeUrl = getAuthorizeUrl(state);

        assertThat(supplementInformationRequester.getThirdPartyAppPayload().isPresent()).isTrue();
        supplementInformationRequester
                .getThirdPartyAppPayload()
                .ifPresent(
                        payload -> {
                            assertThat(payload.getAndroid()).isNotNull();
                            assertThat(payload.getAndroid().getIntent())
                                    .isEqualTo(expectedAuthorizeUrl);
                            assertThat(payload.getIos()).isNotNull();
                            assertThat(payload.getIos().getScheme()).isEqualTo("https");
                            assertThat(payload.getIos().getDeepLinkUrl())
                                    .isEqualTo(expectedAuthorizeUrl);
                            assertThat(payload.getDesktop()).isNotNull();
                            assertThat(payload.getDesktop().getUrl())
                                    .isEqualTo(expectedAuthorizeUrl);
                        });
    }
}
