package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.CodeExchangeForm;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.CodeExchangeResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StarlingApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public StarlingApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public OAuth2Token exchangeCode(CodeExchangeForm codeExchangeForm) {
       return client.request(StarlingConstants.URL.OAUTH2_TOKEN)
                .body(codeExchangeForm, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(CodeExchangeResponse.class)
                .toOauth2Token();
    }

    public AccountsResponse fetchAccounts() {
        return request(StarlingConstants.URL.ACCOUNTS)
                .get(AccountsResponse.class);
    }

    public AccountHolderResponse fetchAccountHolder() {
        return request(StarlingConstants.URL.ACCOUNT_HOLDER)
                .get(AccountHolderResponse.class);
    }

    public AccountIdentifiersResponse fetchAccountIdentifiers(final String accountUid) {
        return request(StarlingConstants.URL.ACCOUNT_IDENTIFIERS(accountUid))
                .get(AccountIdentifiersResponse.class);
    }

    public AccountBalanceResponse fetchAccountBalance(final String accountUid) {
        return request(StarlingConstants.URL.ACCOUNT_BALANCE(accountUid))
                .get(AccountBalanceResponse.class);
    }

    public TransactionsResponse fetchTransactions(Date from, Date to) {

        return request(StarlingConstants.URL.ANY_TRANSACTIONS)
                .queryParam(StarlingConstants.RequestKey.FROM, dateFormat.format(from))
                .queryParam(StarlingConstants.RequestKey.TO, dateFormat.format(to))
                .get(TransactionsResponse.class);
    }

    private RequestBuilder request(String url) {
        return client.request(url)
                .header(HttpHeaders.AUTHORIZATION, this.getBearerHeaderValue())
                .accept(MediaType.APPLICATION_JSON);
    }

    private String getBearerHeaderValue() {

        OAuth2Token token = persistentStorage.get(StarlingConstants.StorageKey.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));

        return token.toAuthorizeHeader();
    }
}
