package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.*;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.*;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration.HandelsbankenBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.BalanceAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
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

    private RequestBuilder createRequest(URL url) {
        return client
                .request(url)
                .header(HeaderKeys.X_IBM_CLIENT_ID, configuration.getAppId())
                .header(HeaderKeys.AUTHORIZATION, BodyKeys.BEARER + sessionStorage.get(HandelsbankenBaseConstants.StorageKeys.ACCESS_TOKEN))
                .header(HeaderKeys.TPP_TRANSACTION_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.TPP_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, configuration.getPsuIpAddress())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON);
    }

    public AccountsResponse getAccountList() {
        return createRequest(new URL(Urls.BASE_URL + Urls.ACCOUNTS))
                .get(AccountsResponse.class);
    }

    public BalanceAccountResponse getAccountDetails(String accountId) {
        return createRequest(new URL(Urls.BASE_URL + String.format(Urls.ACCOUNT_DETAILS, accountId)))
                .queryParam(QueryKeys.WITH_BALANCE, Boolean.TRUE.toString())
                .get(BalanceAccountResponse.class);
    }

    public TransactionResponse getTransactions(String accountId, Date dateFrom, Date dateTo) {
        return createRequest(
                new URL(Urls.BASE_URL + String.format(Urls.ACCOUNT_TRANSACTIONS, accountId)))
                .queryParam(QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(dateFrom))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(dateTo))
                .get(TransactionResponse.class);
    }

    public DecoupledResponse getDecoupled(URL href) {
        return client
                .request(href)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .post(DecoupledResponse.class);
    }

    public TokResponse getBearerTok(String clientId) {
        return client
                .request(new URL(Urls.TOKEN))
                .body(BodyKeys.GRANT_TYPE + "=" + BodyValues.CLIENT_CREDENTIALS + "&" + BodyKeys.SCOPE + "=" + BodyValues.AIS_SCOPE + "&" + BodyKeys.CLIENT_ID + "=" + clientId)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokResponse.class);
    }

    public AuthorizationResponse getAuthorizationToken(String code, String clientId) {

        return client
                .request(new URL(Urls.AUTHORIZATION))
                .body(new AuthorizationRequest(HandelsbankenBaseConstants.BodyValues.ALL_ACCOUNTS))
                .header(HeaderKeys.X_IBM_CLIENT_ID, clientId)
                .header(HeaderKeys.COUNTRY, HandelsbankenBaseConstants.Market.COUNTRY)
                .header(HeaderKeys.AUTHORIZATION, HandelsbankenBaseConstants.BodyKeys.BEARER + code)
                .header(HeaderKeys.PSU_IP_ADDRESS, configuration.getPsuIpAddress())
                .header(HeaderKeys.TPP_TRANSACTION_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.TPP_REQUEST_ID, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .post(AuthorizationResponse.class);
    }

    public SessionResponse getSessionId(String personalId, String consentId) {

        return client
                .request(new URL(Urls.SESSION))
                .header(HeaderKeys.CONSENT_ID, consentId)
                .body(new SessionRequest(configuration.getAppId(), BodyValues.AIS_SCOPE + ":" + consentId, configuration.getPsuIpAddress(), personalId, BodyValues.PERSONAL_ID_TP))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .post(SessionResponse.class);
    }
    
    public HttpResponse cancelDecoupled (URL href) {
        return client.request(href)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .accept(MediaType.APPLICATION_JSON)
            .post(HttpResponse.class);
    }


  public SessionResponse buildAuthorizeUrl(String ssn) {

        TokResponse tokResponse = getBearerTok(configuration.getAppId());
        AuthorizationResponse authResponse = getAuthorizationToken(tokResponse.getAccessToken(), configuration.getAppId());

        SessionResponse sessionResponse = getSessionId(ssn, authResponse.getConsentId());

        return sessionResponse;

    }


}
