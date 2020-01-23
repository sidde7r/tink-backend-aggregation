package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale;

import java.util.Base64;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SocieteGeneraleApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final SocieteGeneraleConfiguration configuration;

    public SocieteGeneraleApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SocieteGeneraleConfiguration configuration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    public TokenResponse exchangeAuthorizationCodeOrRefreshToken(TokenRequest request) {
        return client.request(new URL(SocieteGeneraleConstants.Urls.TOKEN_PATH))
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        SocieteGeneraleConstants.HeaderKeys.AUTHORIZATION,
                        createAuthorizationBasicHeaderValue())
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class);
    }

    public AccountsResponse fetchAccounts(String signature, String reqId) {
        return createRequest(SocieteGeneraleConstants.Urls.ACCOUNTS_PATH, signature, reqId)
                .get(AccountsResponse.class);
    }

    public TransactionsResponse getTransactions(
            String accountId, String signature, String reqId, URL nextPageUrl) {
        RequestBuilder requestBuilder =
                nextPageUrl != null
                        ? createRequest(nextPageUrl, signature, reqId)
                        : createFirstRequest(accountId, signature, reqId);

        return requestBuilder.get(TransactionsResponse.class);
    }

    public EndUserIdentityResponse getEndUserIdentity(String signature, String reqId) {
        return createRequest(Urls.END_USER_IDENTITY_PATH, signature, reqId)
                .get(EndUserIdentityResponse.class);
    }

    private RequestBuilder createFirstRequest(String accountId, String signature, String reqId) {
        return createRequest(
                SocieteGeneraleConstants.Urls.TRANSACTIONS_PATH.parameter(
                        SocieteGeneraleConstants.IdTags.ACCOUNT_RESOURCE_ID, accountId),
                signature,
                reqId);
    }

    private RequestBuilder createRequest(URL url, String signature, String reqId) {
        return client.request(url)
                .header(
                        SocieteGeneraleConstants.HeaderKeys.AUTHORIZATION,
                        createAuthorizationBearerHeaderValue())
                .header(SocieteGeneraleConstants.HeaderKeys.X_REQUEST_ID, reqId)
                .header(SocieteGeneraleConstants.HeaderKeys.SIGNATURE, signature)
                .header(SocieteGeneraleConstants.HeaderKeys.CLIENT_ID, configuration.getClientId())
                .accept(MediaType.APPLICATION_JSON);
    }

    private String createAuthorizationBasicHeaderValue() {
        return new StringBuilder()
                .append(SocieteGeneraleConstants.HeaderValues.BASIC)
                .append(" ")
                .append(Base64.getEncoder().encodeToString(getAuthorizationString().getBytes()))
                .toString();
    }

    private String createAuthorizationBearerHeaderValue() {
        return new StringBuilder()
                .append(SocieteGeneraleConstants.HeaderValues.BEARER)
                .append(" ")
                .append(persistentStorage.get(SocieteGeneraleConstants.StorageKeys.TOKEN))
                .toString();
    }

    private String getAuthorizationString() {
        return configuration.getClientId().concat(":").concat(configuration.getClientSecret());
    }
}
