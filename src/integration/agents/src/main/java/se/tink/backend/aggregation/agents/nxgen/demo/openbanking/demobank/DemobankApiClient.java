package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams.ACCOUNT_ID;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.BASE_URL;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.OAuth2Params;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc.CollectTicketResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc.CreateTicketRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.a2a.rpc.CreateTicketResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.embeddedotp.EmbeddedChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.embeddedotp.EmbeddedCompleteResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.embeddedotp.EmbeddedRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdEnrollEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdGenerateCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdGenerateCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdInstallIdEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdLoginEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdLoginInstallIdEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdLoginWithInstallIdResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities.NemIdResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.NoBankIdCollectRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.NoBankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.NoBankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.NoBankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.PasswordLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.RedirectLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.RedirectRefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.rpc.FetchAccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DemobankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final String callbackUri;

    public DemobankApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, String callbackUri) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.callbackUri = callbackUri;
    }

    public void setTokenToStorage(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH2_TOKEN, accessToken);
    }

    public OAuth2Token getOauth2TokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH2_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Couldn't find token from storage"));
    }

    public URL fetchBaseUrl() {
        return new URL(BASE_URL);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private RequestBuilder createRequestInSession(URL url, OAuth2Token token) {
        return createRequest(url).addBearerToken(token);
    }

    public OAuth2Token getToken(String code) {
        return createRequest(fetchBaseUrl().concat(Urls.OAUTH_TOKEN))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(OAuth2Params.CLIENT_ID, OAuth2Params.CLIENT_SECRET)
                .post(TokenEntity.class, new RedirectLoginRequest(code, callbackUri).toData())
                .toOAuth2Token();
    }

    public OAuth2Token refreshToken(String refreshToken) {
        return createRequest(fetchBaseUrl().concat(Urls.OAUTH_TOKEN))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(OAuth2Params.CLIENT_ID, OAuth2Params.CLIENT_SECRET)
                .post(TokenEntity.class, new RedirectRefreshTokenRequest(refreshToken).toData())
                .toOAuth2Token();
    }

    public OAuth2Token login(String username, String password) {

        return createRequest(fetchBaseUrl().concat(Urls.OAUTH_TOKEN))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(OAuth2Params.CLIENT_ID, OAuth2Params.CLIENT_SECRET)
                .post(TokenEntity.class, new PasswordLoginRequest(username, password).toData())
                .toOAuth2Token();
    }

    public NoBankIdInitResponse initBankIdNo(String ssn, String mobilenumber) {
        return createRequest(fetchBaseUrl().concat(Urls.NO_BANKID_INIT))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(NoBankIdInitResponse.class, new NoBankIdInitRequest(ssn, mobilenumber));
    }

    public NoBankIdCollectResponse collectBankIdNo(String ssn, String sessionId) {
        return createRequest(fetchBaseUrl().concat(Urls.NO_BANKID_COLLECT))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(NoBankIdCollectResponse.class, new NoBankIdCollectRequest(ssn, sessionId));
    }

    private static final String X_NEMID_TOKEN = "X-NemID-Token";

    public NemIdChallengeEntity nemIdGetChallenge(
            NemIdLoginEncryptionEntity encryptionEntity, String token) {
        NemIdResponse nemIdResponse =
                createRequest(fetchBaseUrl().concat(Urls.DK_NEMID_GET_CHALLENGE))
                        .header(X_NEMID_TOKEN, token)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .post(NemIdResponse.class, encryptionEntity);

        return SerializationUtils.deserializeFromString(
                nemIdResponse.getData(), NemIdChallengeEntity.class);
    }

    public NemIdGenerateCodeResponse nemIdGenerateCode(
            NemIdGenerateCodeRequest codeRequest, String token) {
        NemIdResponse nemIdResponse =
                createRequest(fetchBaseUrl().concat(Urls.DK_NEMID_GENERATE_CODE))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(X_NEMID_TOKEN, token)
                        .post(NemIdResponse.class, codeRequest);

        return SerializationUtils.deserializeFromString(
                nemIdResponse.getData(), NemIdGenerateCodeResponse.class);
    }

    public NemIdInstallIdEntity nemIdEnroll(NemIdEnrollEntity entity, String token) {
        NemIdResponse nemIdResponse =
                createRequest(fetchBaseUrl().concat(Urls.DK_NEMID_ENROLL))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(X_NEMID_TOKEN, token)
                        .post(NemIdResponse.class, entity);

        return SerializationUtils.deserializeFromString(
                nemIdResponse.getData(), NemIdInstallIdEntity.class);
    }

    public NemIdLoginWithInstallIdResponse nemIdLoginWithInstallId(
            NemIdLoginInstallIdEncryptionEntity loginWithInstallIdEntity, String token) {
        NemIdResponse nemIdResponse =
                createRequest(fetchBaseUrl().concat(Urls.DK_NEMID_LOGIN))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(X_NEMID_TOKEN, token)
                        .post(NemIdResponse.class, loginWithInstallIdEntity);
        return SerializationUtils.deserializeFromString(
                nemIdResponse.getData(), NemIdLoginWithInstallIdResponse.class);
    }

    public CreateTicketResponse initAppToApp(CreateTicketRequest request) {
        return createRequest(fetchBaseUrl().concat(Urls.A2A_INIT_URL))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(CreateTicketResponse.class, request);
    }

    public CreateTicketResponse initDecoupledAppToApp(String username, String code) {
        CreateTicketRequest request = new CreateTicketRequest(username, null, null, code);
        return createRequest(fetchBaseUrl().concat(Urls.A2A_INIT_DECOUPLED_URL))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(CreateTicketResponse.class, request);
    }

    public CollectTicketResponse collectAppToApp(String ticketId) {
        return createRequest(
                        fetchBaseUrl()
                                .concat(Urls.A2A_COLLECT_URL)
                                .parameter(QueryParams.TICKET_ID, ticketId))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(CollectTicketResponse.class);
    }

    public EmbeddedChallengeResponse initEmbeddedOtp(String username, String password) {
        EmbeddedRequest request = new EmbeddedRequest(username, password, null);
        return createRequest(fetchBaseUrl().concat(Urls.EMBEDDED_OTP_COMMENCE))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(EmbeddedChallengeResponse.class, request);
    }

    public EmbeddedCompleteResponse completeEmbeddedOtp(
            String username, String password, String otp) {
        EmbeddedRequest request = new EmbeddedRequest(username, password, otp);
        return createRequest(fetchBaseUrl().concat(Urls.EMBEDDED_OTP_COMPLETE))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(EmbeddedCompleteResponse.class, request);
    }

    public FetchAccountResponse fetchAccounts() {
        final URL url = fetchBaseUrl().concat(Urls.ACCOUNTS);

        return createRequestInSession(url, getOauth2TokenFromStorage())
                .get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            String accountId, Date fromDate, Date toDate) {
        final URL url =
                fetchBaseUrl()
                        .concat(Urls.TRANSACTIONS)
                        .parameter(ACCOUNT_ID, accountId)
                        .queryParam(
                                DemobankConstants.QueryParams.DATE_FROM,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                        .queryParam(
                                DemobankConstants.QueryParams.DATE_TO,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate));
        return createRequestInSession(url, getOauth2TokenFromStorage())
                .get(FetchTransactionsResponse.class);
    }

    public FetchAccountHolderResponse fetchAccountHolders(String accountId) {
        final URL url = fetchBaseUrl().concat(Urls.HOLDERS).parameter(ACCOUNT_ID, accountId);
        return createRequestInSession(url, getOauth2TokenFromStorage())
                .get(FetchAccountHolderResponse.class);
    }

    public UserEntity fetchUser() {
        final URL url = fetchBaseUrl().concat(Urls.USER);
        return createRequestInSession(url, getOauth2TokenFromStorage()).get(UserEntity.class);
    }
}
