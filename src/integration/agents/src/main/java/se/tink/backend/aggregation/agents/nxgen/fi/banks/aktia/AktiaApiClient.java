package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.AuthenticationIdResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.AuthenticationInitResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.InfoResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.InitiateChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.InitiateChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.LoginDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.Oauth2Request;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.PhoneNumberResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.RegistrationCompleteRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.RegistrationInitResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.RegistrationOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.RegistrationOtpResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.VerifyChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.VerifyChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities.AccountSummaryListEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.rpc.AccountsSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class AktiaApiClient {

    private final TinkHttpClient httpClient;
    private OAuth2Token accessToken;

    public AktiaApiClient(TinkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private String getFirstHeader(HttpResponse response, String headerKey) {
        MultivaluedMap<String, String> headers = response.getHeaders();
        return headers.getFirst(headerKey);
    }

    public RegistrationInitResponse registrationInit(String username, String password)
            throws AuthenticationException {
        Oauth2Request requestBody =
                new Oauth2Request(
                        AktiaConstants.Oauth2Scopes.REGISTRATION_INIT, username, password);

        try {
            HttpResponse response =
                    httpClient
                            .request(AktiaConstants.Url.OAUTH2_REGISTRATION_INIT)
                            .addBasicAuth(
                                    AktiaConstants.HttpHeaders.BASIC_AUTH_USERNAME,
                                    AktiaConstants.HttpHeaders.BASIC_AUTH_PASSWORD)
                            .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                            .post(HttpResponse.class, requestBody);

            RegistrationInitResponse registrationInitResponse =
                    response.getBody(RegistrationInitResponse.class);

            String loginStatus = getFirstHeader(response, AktiaConstants.HttpHeaders.LOGIN_STATUS);
            registrationInitResponse.setLoginStatus(loginStatus);

            String otpCard = getFirstHeader(response, AktiaConstants.HttpHeaders.OTP_CARD);
            registrationInitResponse.setOtpCard(otpCard);

            String otpIndex = getFirstHeader(response, AktiaConstants.HttpHeaders.OTP_INDEX);
            registrationInitResponse.setOtpIndex(otpIndex);

            return registrationInitResponse;
        } catch (HttpResponseException hre) {
            HttpResponse response = hre.getResponse();
            if (response.getStatus() != HttpStatus.SC_BAD_REQUEST) {
                throw hre;
            }

            String loginStatus = getFirstHeader(response, AktiaConstants.HttpHeaders.LOGIN_STATUS);
            String exceptionBody = response.getBody(String.class);
            if (AktiaConstants.ErrorCodes.INVALID_CREDENTIALS.equalsIgnoreCase(loginStatus)
                    || exceptionBody.contains(ErrorCodes.INVALID_GRANT)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(hre);
            }

            throw hre;
        }
    }

    public RegistrationOtpResponse registrationOtpChallengeResponse(
            OAuth2Token token, String otpResponse) {
        RegistrationOtpRequest requestBody = new RegistrationOtpRequest(otpResponse);

        return httpClient
                .request(AktiaConstants.Url.REGISTRATION_INIT)
                .accept(MediaType.WILDCARD)
                .type(MediaType.APPLICATION_JSON)
                .addBearerToken(token)
                .post(RegistrationOtpResponse.class, requestBody);
    }

    public boolean registrationComplete(OAuth2Token token, String encapToken) {
        RegistrationCompleteRequest requestBody = new RegistrationCompleteRequest(encapToken);

        HttpResponse response =
                httpClient
                        .request(AktiaConstants.Url.REGISTRATION_COMPLETE)
                        .accept(MediaType.WILDCARD)
                        .type(MediaType.APPLICATION_JSON)
                        .addBearerToken(token)
                        .post(HttpResponse.class, requestBody);

        return response.getStatus() == HttpStatus.SC_OK;
    }

    public AuthenticationInitResponse authenticationInit(String encapToken) {
        Oauth2Request requestBody =
                new Oauth2Request(
                        AktiaConstants.Oauth2Scopes.AUTHENTICATION_INIT,
                        AktiaConstants.HttpParameters.OAUTH2_USERNAME,
                        encapToken);

        HttpResponse response =
                httpClient
                        .request(AktiaConstants.Url.OAUTH2_AUTHENTICATION_INIT)
                        .addBasicAuth(
                                AktiaConstants.HttpHeaders.BASIC_AUTH_USERNAME,
                                AktiaConstants.HttpHeaders.BASIC_AUTH_PASSWORD)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(HttpResponse.class, requestBody);

        AuthenticationInitResponse authenticationInitResponse =
                response.getBody(AuthenticationInitResponse.class);

        String loginStatus = getFirstHeader(response, AktiaConstants.HttpHeaders.LOGIN_STATUS);
        authenticationInitResponse.setLoginStatus(loginStatus);

        return authenticationInitResponse;
    }

    public InfoResponse getAvainInfo(OAuth2Token token) {
        return httpClient
                .request(AktiaConstants.Url.AVAIN_INFO)
                .accept(MediaType.WILDCARD)
                .addBearerToken(token)
                .get(InfoResponse.class);
    }

    public PhoneNumberResponse getPhoneInfo(OAuth2Token token) {
        return httpClient
                .request(AktiaConstants.Url.GET_PHONE_NUMBER)
                .accept(MediaType.WILDCARD)
                .addBearerToken(token)
                .get(PhoneNumberResponse.class);
    }

    public InitiateChallengeResponse initiateChallenge(OAuth2Token token) {
        return httpClient
                .request(Url.INITIATE_CHALLENGE)
                .body(new InitiateChallengeRequest(), MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.WILDCARD)
                .addBearerToken(token)
                .post(InitiateChallengeResponse.class);
    }

    public VerifyChallengeResponse verifyChallenge(OAuth2Token token, String code) {
        return httpClient
                .request(Url.VERIFY_CHALLENGE)
                .body(new VerifyChallengeRequest(code), MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.WILDCARD)
                .addBearerToken(token)
                .post(VerifyChallengeResponse.class);
    }

    public Optional<String> getAuthenticationId(OAuth2Token token) {
        AuthenticationIdResponse authenticationIdResponse =
                httpClient
                        .request(AktiaConstants.Url.AUTHENTICATION_INIT)
                        .body("{}", MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.WILDCARD)
                        .addBearerToken(token)
                        .post(AuthenticationIdResponse.class);

        return authenticationIdResponse.getId();
    }

    public OAuth2Token getAndSaveAuthenticatedToken(String encapToken) {
        Oauth2Request requestBody =
                new Oauth2Request(
                        AktiaConstants.Oauth2Scopes.AUTHENTICATION_COMPLETE,
                        AktiaConstants.HttpParameters.OAUTH2_USERNAME,
                        encapToken);

        TokenResponse token =
                httpClient
                        .request(AktiaConstants.Url.OAUTH2_AUTHENTICATION_COMPLETE)
                        .addBasicAuth(
                                AktiaConstants.HttpHeaders.BASIC_AUTH_USERNAME,
                                AktiaConstants.HttpHeaders.BASIC_AUTH_PASSWORD)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(TokenResponse.class, requestBody);

        // Save it for normal api access.
        this.accessToken = token.getToken();

        return this.accessToken;
    }

    public LoginDetailsResponse getLoginDetails() {
        return httpClient
                .request(AktiaConstants.Url.LOGIN_DETAILS)
                .addBearerToken(accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .get(LoginDetailsResponse.class);
    }

    public List<AccountSummaryListEntity> getAccountList() {
        return httpClient
                .request(AktiaConstants.Url.ACCOUNT_LIST_0)
                .addBearerToken(accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .get(AccountsSummaryResponse.class)
                .getAccountSummary()
                .getAccountSummaryList();
    }

    public TransactionsResponse getAccountTransactions(String aktiaAccountId) {
        return httpClient
                .request(
                        AktiaConstants.Url.ACCOUNT_TRANSACTIONS.parameter(
                                AktiaConstants.HttpParameters.ACCOUNT_ID, aktiaAccountId))
                .addBearerToken(accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .get(TransactionsResponse.class);
    }

    public TransactionsResponse getAccountTransactionsWithPageKey(
            String aktiaAccountId, String pageKey) {
        return httpClient
                .request(
                        AktiaConstants.Url.ACCOUNT_TRANSACTIONS
                                .parameter(AktiaConstants.HttpParameters.ACCOUNT_ID, aktiaAccountId)
                                .queryParam(AktiaConstants.HttpParameters.PAGE_KEY, pageKey))
                .addBearerToken(accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .get(TransactionsResponse.class);
    }
}
