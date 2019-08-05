package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.SamlinkConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.configuration.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponseWithoutHref;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenRequestPost;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponseBerlinGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SamlinkApiClient extends BerlinGroupApiClient<SamlinkConfiguration> {

    public SamlinkApiClient(final TinkHttpClient client, final SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public URL getAuthorizeUrl(final String state) {
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
    public AccountsBaseResponseBerlinGroup fetchAccounts() {
        return getAccountsRequestBuilder(getConfiguration().getBaseUrl() + Urls.ACCOUNTS)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(
                        SamlinkConstants.HeaderKeys.SUBSCRIPTION_KEY,
                        getConfiguration().getSubscriptionKey())
                .get(AccountsBaseResponseBerlinGroup.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(final String url) {
        return createRequest(new URL(url)).get(TransactionsKeyPaginatorBaseResponse.class);
    }

    @Override
    public OAuth2Token getToken(final String code) {
        final TokenRequestPost tokenRequest =
                new TokenRequestPost(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        getConfiguration().getRedirectUrl(),
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        "");

        return createRequest(new URL(getConfiguration().getBaseUrl() + Urls.TOKEN))
                .body(tokenRequest.toData(), MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(
                        getConfiguration().getClientId(), getConfiguration().getClientSecret())
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public String getConsentId() {
        AccessEntity accessEntity = new AccessEntity.Builder().build();
        final ConsentBaseRequest consentsRequest = new ConsentBaseRequest();
        consentsRequest.setAccess(accessEntity);

        return createRequest(new URL(getConfiguration().getBaseUrl() + Urls.CONSENT))
                .body(consentsRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                .post(ConsentBaseResponseWithoutHref.class)
                .getConsentId();
    }

    @Override
    public OAuth2Token refreshToken(final String token) {
        return null;
    }

    private RequestBuilder createRequest(final URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(
                        SamlinkConstants.HeaderKeys.SUBSCRIPTION_KEY,
                        getConfiguration().getSubscriptionKey())
                .type(MediaType.APPLICATION_JSON);
    }
}
