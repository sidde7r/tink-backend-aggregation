package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.BodyKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.BodyValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.UrlParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration.HandelsbankenBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.BalanceAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.UUID;

public class HandelsbankenBaseApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private HandelsbankenBaseConfiguration configuration;

    public HandelsbankenBaseApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public void setConfiguration(HandelsbankenBaseConfiguration configuration) {
        this.configuration = configuration;
    }

    public HandelsbankenBaseConfiguration getConfiguration() {
        return this.configuration;
    }

    private OAuth2Token getOauthFromSession(){
        return sessionStorage.get(HandelsbankenBaseConstants.StorageKeys.ACCESS_TOKEN, OAuth2Token.class)
                .orElseThrow(()-> new IllegalStateException(HandelsbankenBaseConstants.ExceptionMessages.TOKEN_NOT_FOUND));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(HeaderKeys.X_IBM_CLIENT_ID, this.configuration.getAppId())
                .addBearerToken(getOauthFromSession())
                .header(HeaderKeys.TPP_TRANSACTION_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.TPP_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, configuration.getPsuIpAddress())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON);
    }

    public AccountsResponse getAccountList() {
        return createRequest(Urls.ACCOUNTS).get(AccountsResponse.class);
    }

    public BalanceAccountResponse getAccountDetails(String accountId) {
        return createRequest(Urls.ACCOUNT_DETAILS.parameter(UrlParams.ACCOUNT_ID, accountId))
                .queryParam(QueryKeys.WITH_BALANCE, Boolean.TRUE.toString())
                .get(BalanceAccountResponse.class);
    }

    public TransactionResponse getTransactions(String accountId, Date dateFrom, Date dateTo) {
        return createRequest(Urls.ACCOUNT_TRANSACTIONS.parameter(UrlParams.ACCOUNT_ID, accountId))
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(dateFrom))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(dateTo))
                .get(TransactionResponse.class);
    }

    public DecoupledResponse getDecoupled(URL href) {
        return client.request(href)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .post(DecoupledResponse.class);
    }

    private TokenResponse getBearerToken(String clientId) {

        final Form params =
                Form.builder()
                        .put(BodyKeys.GRANT_TYPE, BodyValues.CLIENT_CREDENTIALS)
                        .put(BodyKeys.SCOPE, BodyValues.AIS_SCOPE)
                        .put(BodyKeys.CLIENT_ID, clientId)
                        .build();

        return client.request(Urls.TOKEN)
                .body(params.toString())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponse.class);
    }

    private AuthorizationResponse getAuthorizationToken(String code, String clientId) {

        return client.request(Urls.AUTHORIZATION)
                .body(new AuthorizationRequest(HandelsbankenBaseConstants.BodyValues.ALL_ACCOUNTS))
                .header(HeaderKeys.X_IBM_CLIENT_ID, clientId)
                .header(HeaderKeys.COUNTRY, HandelsbankenBaseConstants.Market.COUNTRY)
                .header(HeaderKeys.AUTHORIZATION, HandelsbankenBaseConstants.HeaderKeys.BEARER + code)
                .header(HeaderKeys.PSU_IP_ADDRESS, configuration.getPsuIpAddress())
                .header(HeaderKeys.TPP_TRANSACTION_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.TPP_REQUEST_ID, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .post(AuthorizationResponse.class);
    }



    public SessionResponse getSessionId(String personalId, String consentId) {

        return client.request(Urls.SESSION)
                .header(HeaderKeys.CONSENT_ID, consentId)
                .body(
                        new SessionRequest(
                                configuration.getAppId(),
                                BodyValues.AIS_SCOPE + ":" + consentId,
                                configuration.getPsuIpAddress(),
                                personalId,
                                BodyValues.PERSONAL_ID_TP))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .post(SessionResponse.class);
    }

    public SessionResponse getSession(String ssn) {

        TokenResponse tokenResponse = getBearerToken(configuration.getAppId());
        AuthorizationResponse authResponse =
                getAuthorizationToken(tokenResponse.getAccessToken(), configuration.getAppId());

        return getSessionId(ssn, authResponse.getConsentId());
    }

    public TokenResponse getRefreshToken() {

        final Form params =
                Form.builder()
                        .put(BodyKeys.GRANT_TYPE, BodyValues.REFRESH_TOKEN)
                        .put(BodyKeys.REFRESH_TOKEN, sessionStorage.get(HandelsbankenBaseConstants.StorageKeys.REFRESH_TOKEN))
                        .put(BodyKeys.CLIENT_ID, configuration.getAppId())
                        .build();

        return client.request(Urls.TOKEN)
                .body(params.toString())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponse.class);
    }
}
