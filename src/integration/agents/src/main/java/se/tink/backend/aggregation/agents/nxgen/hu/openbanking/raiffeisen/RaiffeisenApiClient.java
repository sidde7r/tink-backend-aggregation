package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
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
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        RaiffeisenConstants.ErrorMessages.MISSING_CONFIGURATION));
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
                .header(
                        RaiffeisenConstants.HeaderKeys.CONSENT_ID,
                        sessionStorage.get(RaiffeisenConstants.StorageKeys.CONSENT_ID))
                .header(RaiffeisenConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(
                        RaiffeisenConstants.HeaderKeys.X_IBM_CLIENT_ID,
                        configuration.getClientId());
    }

    public GetConsentResponse getConsent(GetConsentRequest getConsentRequest) {
        return createRequest(RaiffeisenConstants.Urls.GET_CONSENT)
                .header(RaiffeisenConstants.HeaderKeys.X_IBM_CLIENT_ID, configuration.getClientId())
                .header(RaiffeisenConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .post(GetConsentResponse.class, getConsentRequest);
    }

    public URL getUrl(String state, GetConsentResponse getConsentResponse) {
        return createRequest(RaiffeisenConstants.Urls.AUTHORIZE)
                .queryParam(RaiffeisenConstants.QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(
                        RaiffeisenConstants.QueryKeys.RESPONSE_TYPE,
                        RaiffeisenConstants.QueryValues.CODE)
                .queryParam(
                        RaiffeisenConstants.QueryKeys.SCOPE, RaiffeisenConstants.QueryValues.AISP)
                .queryParam(
                        RaiffeisenConstants.QueryKeys.REDIRECT_URI, configuration.getRedirectUri())
                .queryParam(
                        RaiffeisenConstants.QueryKeys.CONSENT_ID, getConsentResponse.getConsentId())
                .queryParam(RaiffeisenConstants.QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(GetTokenForm getTokenForm) {
        return client.request(RaiffeisenConstants.Urls.GET_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .body(getTokenForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token refreshToken(RefreshTokenForm refreshTokenForm) {
        return client.request(RaiffeisenConstants.Urls.GET_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .body(refreshTokenForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public Collection<TransactionalAccount> getAccounts() {
        GetAccountsResponse accounts =
                createFetchingRequest(RaiffeisenConstants.Urls.GET_ACCOUNTS)
                        .queryParam(
                                RaiffeisenConstants.QueryKeys.WITH_BALANCE,
                                RaiffeisenConstants.QueryValues.TRUE)
                        .get(GetAccountsResponse.class);
        return accounts.toTinkAccounts();
    }

    private Optional<OAuth2Token> getToken() {
        return sessionStorage.get(RaiffeisenConstants.StorageKeys.TOKEN, OAuth2Token.class);
    }

    public PaginatorResponse getTransactions(TransactionalAccount account, int page) {
        return createFetchingRequest(
                        RaiffeisenConstants.Urls.GET_TRANSACTIONS.parameter(
                                RaiffeisenConstants.IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(
                        RaiffeisenConstants.QueryKeys.DATE_FROM,
                        RaiffeisenConstants.QueryValues.DATE_FROM)
                .queryParam(
                        RaiffeisenConstants.QueryKeys.BOOKING_STATUS,
                        RaiffeisenConstants.QueryValues.BOTH)
                .queryParam(RaiffeisenConstants.QueryKeys.PAGE, String.valueOf(page))
                .get(GetTransactionsResponse.class);
    }
}
