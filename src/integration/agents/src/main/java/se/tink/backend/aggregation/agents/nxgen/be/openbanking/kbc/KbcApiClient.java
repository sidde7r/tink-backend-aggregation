package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenRequestPost;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class KbcApiClient extends BerlinGroupApiClient {

    private Credentials credentials;

    public KbcApiClient(
            TinkHttpClient client, SessionStorage sessionStorage, Credentials credentials) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public AccountsBaseResponse fetchAccounts() {
        return getAccountsRequestBuilder(getConfiguration().getBaseUrl() + Urls.ACCOUNTS)
                .get(AccountsBaseResponse.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(String url) {
        return getTransactionsRequestBuilder(
                        getConfiguration().getBaseUrl() + Urls.AIS_PRODUCT + url)
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        final String consentId = getConsentId();
        sessionStorage.put(StorageKeys.CONSENT_ID, consentId);
        final String authUrl = getConfiguration().getBaseUrl() + Urls.AUTH;
        return getAuthorizeUrlWithCode(
                        authUrl,
                        state,
                        consentId,
                        getConfiguration().getClientId(),
                        getConfiguration().getRedirectUrl())
                .getUrl();
    }

    @Override
    public OAuth2Token getToken(String code) {
        final TokenRequestPost tokenRequest =
                new TokenRequestPost(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        getConfiguration().getRedirectUrl(),
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        sessionStorage.get(StorageKeys.CODE_VERIFIER));

        return client.request(getConfiguration().getBaseUrl() + Urls.TOKEN)
                .addBasicAuth(
                        getConfiguration().getClientId(), getConfiguration().getClientSecret())
                .body(tokenRequest.toData(), MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public OAuth2Token refreshToken(String token) {
        final RefreshTokenRequest refreshTokenRequest =
                new RefreshTokenRequest(
                        FormValues.REFRESH_TOKEN_GRANT_TYPE,
                        token,
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret());

        return client.request(getConfiguration().getBaseUrl() + Urls.TOKEN)
                .addBasicAuth(
                        getConfiguration().getClientId(), getConfiguration().getClientSecret())
                .body(refreshTokenRequest.toData(), MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public String getConsentId() {
        final ConsentRequest consentsRequest = new ConsentRequest();
        consentsRequest.getAccess().addIban(credentials.getField("IBAN"));
        return client.request(getConfiguration().getBaseUrl() + Urls.CONSENT)
                .body(consentsRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                .post(ConsentBaseResponse.class)
                .getConsentId();
    }
}
