package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import com.google.api.client.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.ExchangeAuthorizationCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalAccountResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

import javax.ws.rs.core.MediaType;
import java.util.Collection;

public class RabobankApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public RabobankApiClient(final TinkHttpClient client, final PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public TokenResponse exchangeAuthorizationCode(final ExchangeAuthorizationCodeRequest request) {
        return post(request);
    }

    public TokenResponse refreshAccessToken(final RefreshTokenRequest request) {
        return post(request);
    }

    private TokenResponse post(final AbstractForm request) {
        return client.request(RabobankConstants.URL.OAUTH2_TOKEN_RABOBANK)
                .body(request, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header("authorization", "")
                .addBasicAuth(getClientId(), getClientSecret())
                .post(TokenResponse.class);
    }

    private String getClientId() {
        final String clientId = persistentStorage.get(RabobankConstants.StorageKey.CLIENT_ID);
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalStateException("clientId is null or empty!");
        }

        return clientId;
    }

    private String getClientSecret() {
        final String clientId = persistentStorage.get(RabobankConstants.StorageKey.CLIENT_SECRET);
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalStateException("client secret is null or empty!");
        }

        return clientId;
    }

    public Collection<TransactionalAccount> fetchAccounts() {
        client.request(RabobankConstants.URL.AIS_RABOBANK_ACCOUNTS)
                .header(RabobankConstants.QueryParams.TPP_SIGNATURE_CERTIFICATE, "")
                .header(RabobankConstants.QueryParams.REQUEST_ID, "")
                .header(RabobankConstants.QueryParams.DIGEST, "")
                .header(RabobankConstants.QueryParams.SIGNATURE, "")
                .header(RabobankConstants.QueryParams.DATE, "")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(TransactionalAccountResponse.class);
        return null;
    }
}
