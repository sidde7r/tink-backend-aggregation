package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SpareBank1ApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public SpareBank1ApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(
                        SpareBank1Constants.HeaderKeys.CACHE_CONTROL,
                        SpareBank1Constants.HeaderValues.CACHE_CONTROL);
    }

    private RequestBuilder createRequestInSession(URL url) {
        OAuth2Token token = getTokenFromSession();
        return createRequest(url)
                .header(
                        SpareBank1Constants.HeaderKeys.AUTHORIZATION,
                        token.getTokenType() + " " + token.getAccessToken());
    }

    public OAuth2Token getToken(GetTokenForm form) {
        return createRequest(SpareBank1Constants.Urls.FETCH_TOKEN)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(SpareBank1Constants.Urls.FETCH_ACCOUNTS)
                .get(GetAccountsResponse.class);
    }

    public GetTransactionsResponse getTransactions(TransactionalAccount account) {
        return createRequestInSession(
                        SpareBank1Constants.Urls.FETCH_TRANSACTIONS.parameter(
                                SpareBank1Constants.IdTags.ACCOUNT_ID,
                                account.getFromTemporaryStorage(
                                        SpareBank1Constants.StorageKeys.ACCOUNT_ID)))
                .get(GetTransactionsResponse.class);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(SpareBank1Constants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }
}
