package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.DecoupledAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.DecoupledAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.DecoupledTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration.SebConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.filter.SebBankFailureFilter;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public abstract class SebBaseApiClient {

    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected SebConfiguration configuration;
    private boolean isManualRequest;

    public SebBaseApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, boolean isManualRequest) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        client.addFilter(new SebBankFailureFilter());
        client.addFilter(
                new TimeoutRetryFilter(
                        HttpClient.NO_RESPONSE_MAX_RETRIES,
                        HttpClient.NO_RESPONSE_SLEEP_MILLISECONDS));
        this.isManualRequest = isManualRequest;
    }

    public void setConfiguration(SebConfiguration configuration) {
        this.configuration = configuration;
    }

    public DecoupledAuthResponse startDecoupledAuthorization(
            DecoupledAuthRequest authorizationRequest) {
        return client.request(Urls.DECOUPLED_AUTHORIZATION)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(authorizationRequest, MediaType.APPLICATION_JSON_TYPE)
                .post(DecoupledAuthResponse.class);
    }

    public DecoupledAuthResponse getDecoupledAuthStatus(String authRequestId) {
        return client.request(
                        Urls.DECOUPLED_AUTHORIZATION
                                .concat(URL.URL_SEPARATOR)
                                .concat(authRequestId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(DecoupledAuthResponse.class);
    }

    public TokenResponse getDecoupledToken(DecoupledTokenRequest tokenRequest) {
        return client.request(Urls.DECOUPLED_TOKEN)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE)
                .post(TokenResponse.class);
    }

    public abstract RequestBuilder getAuthorizeUrl();

    public abstract AuthorizeResponse getAuthorization(String clientId, String redirectUri);

    public abstract AuthorizeResponse postAuthorization(final String requestForm);

    public abstract OAuth2Token getToken(TokenRequest request);

    public SebConfiguration getConfiguration() {
        return configuration;
    }

    protected OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    public abstract FetchCardAccountsTransactions fetchCardTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate);

    public RequestBuilder buildCardTransactionsFetch(
            URL url, LocalDate fromDate, LocalDate toDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SebCommonConstants.DATE_FORMAT);

        return createRequestInSession(url)
                .queryParam(SebCommonConstants.QueryKeys.DATE_FROM, fromDate.format(formatter))
                .queryParam(SebCommonConstants.QueryKeys.DATE_TO, toDate.format(formatter))
                .queryParam(
                        SebCommonConstants.QueryKeys.BOOKING_STATUS,
                        SebCommonConstants.QueryValues.PENDING_AND_BOOKED_TRANSACTIONS);
    }

    public abstract FetchCardAccountResponse fetchCardAccounts();

    protected RequestBuilder createRequestInSession(URL url) {
        RequestBuilder requestBuilder =
                createRequest(url)
                        .header(
                                SebCommonConstants.HeaderKeys.X_REQUEST_ID,
                                Psd2Headers.getRequestId())
                        .addBearerToken(getTokenFromStorage());
        if (isManualRequest) {
            requestBuilder.header(
                    SebCommonConstants.HeaderKeys.PSU_IP_ADDRESS,
                    SebCommonConstants.getPsuIpAddress());
        }
        return requestBuilder;
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public OAuth2Token refreshToken(String url, RefreshRequest request) throws SessionException {
        return client.request(new URL(url))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }
}
