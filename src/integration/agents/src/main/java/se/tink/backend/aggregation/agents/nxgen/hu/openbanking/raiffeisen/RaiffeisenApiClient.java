package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.RaiffeisenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc.GetConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc.GetConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.configuration.RaiffeisenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class RaiffeisenApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private RaiffeisenConfiguration configuration;

    public RaiffeisenApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public RaiffeisenConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(RaiffeisenConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        OAuth2Token token = getToken().get();
        return createRequest(url).addBearerToken(token);
    }

    private RequestBuilder createFetchingRequest(URL url) {
        return createRequestInSession(url)
                .header(HeaderKeys.CONSENT_ID, sessionStorage.get(StorageKeys.CONSENT_ID))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.X_IBM_CLIENT_ID, configuration.getClientId());
    }

    public GetConsentResponse getConsent(GetConsentRequest getConsentRequest) {
        return createRequest(Urls.GET_CONSENT)
                .header(HeaderKeys.X_IBM_CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .post(GetConsentResponse.class, getConsentRequest);
    }

    public URL getUrl(String state, GetConsentResponse getConsentResponse) {
        return createRequest(Urls.AUTHORIZE)
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.SCOPE, QueryValues.AISP)
                .queryParam(QueryKeys.REDIRECT_URI, configuration.getRedirectUri())
                .queryParam(QueryKeys.CONSENT_ID, getConsentResponse.getConsentId())
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(GetTokenForm getTokenForm) {
        return client.request(Urls.GET_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .body(getTokenForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token refreshToken(RefreshTokenForm refreshTokenForm) {
        return client.request(Urls.GET_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .body(refreshTokenForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public Collection<TransactionalAccount> getAccounts() {
        GetAccountsResponse accounts =
                createFetchingRequest(Urls.GET_ACCOUNTS)
                        .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                        .get(GetAccountsResponse.class);
        return accounts.toTinkAccounts();
    }

    private Optional<OAuth2Token> getToken() {
        return sessionStorage.get(StorageKeys.TOKEN, OAuth2Token.class);
    }

    public PaginatorResponse getTransactions(TransactionalAccount account, int page) {
        return createFetchingRequest(
                        Urls.GET_TRANSACTIONS.parameter(
                                IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(QueryKeys.DATE_FROM, QueryValues.DATE_FROM)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(QueryKeys.PAGE, String.valueOf(page))
                .get(GetTransactionsResponse.class);
    }
}
