package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants.Urls;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.configuration.SpareBank1Configuration;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SpareBank1ApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private SpareBank1Configuration configuration;

    public SpareBank1ApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public SpareBank1Configuration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(SpareBank1Configuration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url).header(HeaderKeys.CACHE_CONTROL, HeaderValues.CACHE_CONTROL);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token token = getTokenFromSession();

        return createRequest(url).addBearerToken(token);
    }

    public OAuth2Token getToken(GetTokenForm form) {
        return createRequest(Urls.FETCH_TOKEN)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(Urls.FETCH_ACCOUNTS).get(GetAccountsResponse.class);
    }

    public GetTransactionsResponse getTransactions(TransactionalAccount account) {
        final String accountId = account.getFromTemporaryStorage(StorageKeys.ACCOUNT_ID);
        final URL url = Urls.FETCH_TRANSACTIONS.parameter(IdTags.ACCOUNT_ID, accountId);

        return createRequestInSession(url).get(GetTransactionsResponse.class);
    }

    private OAuth2Token getTokenFromSession() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }
}
