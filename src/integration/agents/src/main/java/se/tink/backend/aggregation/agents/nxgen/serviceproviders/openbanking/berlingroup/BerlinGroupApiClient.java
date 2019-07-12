package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponseBerlinGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.BerlinGroupAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public abstract class BerlinGroupApiClient<TConfiguration extends BerlinGroupConfiguration> {
    protected TinkHttpClient client;
    protected SessionStorage sessionStorage;
    private TConfiguration configuration;

    public abstract BerlinGroupAccountResponse fetchAccounts();

    public abstract TransactionsKeyPaginatorBaseResponse fetchTransactions(String url);

    public abstract OAuth2Token getToken(String code);

    public abstract String getConsentId();

    public abstract OAuth2Token refreshToken(String token);

    public TConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final TConfiguration configuration) {
        this.configuration = configuration;
    }

    public RequestBuilder getAccountsRequestBuilder(final String url) {
        return client.request(url)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN))
                .header(HeaderKeys.CONSENT_ID, sessionStorage.get(StorageKeys.CONSENT_ID))
                .type(MediaType.APPLICATION_JSON);
    }

    public BerlinGroupAccountResponse fetchAccounts(final String url, final String webApiKey) {
        return getAccountsRequestBuilder(url)
                .header(HeaderKeys.WEB_API_KEY, webApiKey)
                .get(AccountsBaseResponseBerlinGroup.class);
    }

    public RequestBuilder getTransactionsRequestBuilder(final String url) {
        return client.request(url)
                .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .header(HeaderKeys.CONSENT_ID, sessionStorage.get(StorageKeys.CONSENT_ID));
    }

    protected RequestBuilder getPaymentRequestBuilder(final URL url) {
        return client.request(url)
                .addBearerToken(getTokenFromSession(StorageKeys.OAUTH_TOKEN))
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.TPP_REDIRECT_URI, getConfiguration().getRedirectUrl())
                .header(HeaderKeys.CONSENT_ID, sessionStorage.get(StorageKeys.CONSENT_ID))
                .header(HeaderKeys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress());
    }

    public abstract URL getAuthorizeUrl(String state);

    protected RequestBuilder getAuthorizeUrl(
            final String url, final String state, final String clientId, final String redirectUrl) {
        return client.request(url)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam(QueryKeys.STATE, state);
    }

    protected RequestBuilder getAuthorizeUrlWithCode(
            final String url,
            final String state,
            final String consentId,
            final String clientId,
            final String redirectUrl) {
        final String codeVerifier = BerlinGroupUtils.generateCodeVerifier();

        sessionStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);
        final String codeChallenge = BerlinGroupUtils.generateCodeChallenge(codeVerifier);

        return getAuthorizeUrl(url, state, clientId, redirectUrl)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE + consentId)
                .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD);
    }

    protected OAuth2Token getTokenFromSession(final String storageKey) {
        return sessionStorage
                .get(storageKey, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    public void setTokenToSession(final OAuth2Token token, final String storageKey) {
        sessionStorage.put(storageKey, token);
    }
}
