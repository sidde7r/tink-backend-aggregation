package se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.authenticator.rpc.ExchangeRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class MonzoApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public MonzoApiClient(TinkHttpClient client, PersistentStorage storage) {
        this.client = client;
        this.persistentStorage = storage;
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

        String bearerHeaderValue = this.getBearerHeaderValue();

        AccountsResponse accounts = client.request(MonzoConstants.URL.AIS_ACCOUNTS)
                .header(HttpHeaders.AUTHORIZATION, bearerHeaderValue)
                .accept(MediaType.APPLICATION_JSON)
                .get(AccountsResponse.class);

        for (AccountEntity account : accounts.getAccounts()) {

            BalanceEntity balance = client.request(MonzoConstants.URL.AIS_BALANCE)
                    .header(HttpHeaders.AUTHORIZATION, bearerHeaderValue)
                    .queryParam(MonzoConstants.RequestKey.ACCOUNT_ID, account.getId())
                    .accept(MediaType.APPLICATION_JSON)
                    .get(BalanceEntity.class);

            account.setBalance(balance);
        }

        return accounts;
    }

    public TransactionsResponse fetchTransactions(String accountId, Object since, Object before, int fetchLimit) {

        RequestBuilder builder = client.request(MonzoConstants.URL.AIS_TRANSACTIONS)
                .header(HttpHeaders.AUTHORIZATION, this.getBearerHeaderValue())
                .queryParam(MonzoConstants.RequestKey.ACCOUNT_ID, accountId)
                .queryParam(MonzoConstants.RequestKey.LIMIT, Integer.toString(fetchLimit))
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

        OAuth2Token token = persistentStorage.get(MonzoConstants.StorageKey.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));

        return "Bearer " + token.getAccessToken();
    }

}
