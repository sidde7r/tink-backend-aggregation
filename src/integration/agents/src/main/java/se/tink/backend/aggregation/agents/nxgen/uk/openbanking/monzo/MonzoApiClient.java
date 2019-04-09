package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo;

import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator.rpc.ExchangeRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.configuration.MonzoConfiguration;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class MonzoApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private MonzoConfiguration configuration;

    public MonzoApiClient(
            TinkHttpClient client, PersistentStorage storage) {
        this.client = client;
        this.persistentStorage = storage;
    }

    public MonzoConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(MonzoConfiguration configuration) {
        this.configuration = configuration;
    }

    public TokenResponse exchangeAuthorizationCode(ExchangeRequest body) {
        return client.request(MonzoConstants.URL.OAUTH2_TOKEN)
                .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(TokenResponse.class);
    }

    public TokenResponse refreshAccessToken(RefreshRequest request) {
        return client.request(MonzoConstants.URL.OAUTH2_TOKEN)
                .body(request, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(TokenResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        return client.request(MonzoConstants.URL.AIS_ACCOUNTS)
                .header(HttpHeaders.AUTHORIZATION, this.getBearerHeaderValue())
                .accept(MediaType.APPLICATION_JSON)
                .get(AccountsResponse.class);
    }

    public BalanceResponse fetchBalance(String accountId) {
        return client.request(MonzoConstants.URL.AIS_BALANCE)
                .header(HttpHeaders.AUTHORIZATION, this.getBearerHeaderValue())
                .queryParam(MonzoConstants.RequestKey.ACCOUNT_ID, accountId)
                .accept(MediaType.APPLICATION_JSON)
                .get(BalanceResponse.class);
    }

    public TransactionsResponse fetchTransactions(
            String accountId, Object since, Object before, int limit) {
        final RequestBuilder builder =
                client.request(MonzoConstants.URL.AIS_TRANSACTIONS)
                        .header(HttpHeaders.AUTHORIZATION, this.getBearerHeaderValue())
                        .queryParam(MonzoConstants.RequestKey.ACCOUNT_ID, accountId)
                        .queryParam(MonzoConstants.RequestKey.LIMIT, Integer.toString(limit))
                        .accept(MediaType.APPLICATION_JSON);

        if (since != null) {
            builder.queryParam(MonzoConstants.RequestKey.SINCE, since.toString());
        }
        if (before != null) {
            builder.queryParam(MonzoConstants.RequestKey.BEFORE, before.toString());
        }

        return builder.get(TransactionsResponse.class);
    }

    private String getBearerHeaderValue() {
        final OAuth2Token token =
                persistentStorage
                        .get(MonzoConstants.StorageKey.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SessionError.SESSION_EXPIRED.exception()));

        return "Bearer " + token.getAccessToken();
    }
}
