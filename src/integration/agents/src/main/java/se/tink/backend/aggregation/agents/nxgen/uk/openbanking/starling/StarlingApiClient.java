package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.CodeExchangeForm;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.OAuthTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.rpc.TokenRefreshForm;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Date;

public class StarlingApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public StarlingApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public OAuth2Token exchangeCode(CodeExchangeForm codeExchangeForm) {
        return client.request(StarlingConstants.Url.GET_OAUTH2_TOKEN)
                .body(codeExchangeForm, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(OAuthTokenResponse.class)
                .toOauth2Token();
    }

    public OAuth2Token refreshAccessToken(TokenRefreshForm refreshForm) {
        return client.request(StarlingConstants.Url.GET_OAUTH2_TOKEN)
                .body(refreshForm, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(OAuthTokenResponse.class)
                .toOauth2Token();
    }

    public AccountsResponse fetchAccounts() {
        return request(StarlingConstants.Url.GET_ACCOUNTS).get(AccountsResponse.class);
    }

    public AccountHolderResponse fetchAccountHolder() {
        return request(StarlingConstants.Url.GET_ACCOUNT_HOLDER).get(AccountHolderResponse.class);
    }

    public AccountIdentifiersResponse fetchAccountIdentifiers(final String accountUid) {
        return request(
                        StarlingConstants.Url.GET_ACCOUNT_IDENTIFIERS.parameter(
                                StarlingConstants.UrlParams.UID, accountUid))
                .get(AccountIdentifiersResponse.class);
    }

    public AccountBalanceResponse fetchAccountBalance(final String accountUid) {
        return request(
                        StarlingConstants.Url.GET_ACCOUNT_BALANCE.parameter(
                                StarlingConstants.UrlParams.UID, accountUid))
                .get(AccountBalanceResponse.class);
    }

    public TransactionsResponse fetchTransactions(Date from, Date to) {

        return request(StarlingConstants.Url.GET_ANY_TRANSACTIONS)
                .queryParam(StarlingConstants.RequestKey.FROM, toFormattedDate(from))
                .queryParam(StarlingConstants.RequestKey.TO, toFormattedDate(to))
                .get(TransactionsResponse.class);
    }

    private RequestBuilder request(URL url) {
        return client.request(url)
                .header(HttpHeaders.AUTHORIZATION, this.getBearerHeaderValue())
                .accept(MediaType.APPLICATION_JSON);
    }

    private String getBearerHeaderValue() {

        OAuth2Token token =
                persistentStorage
                        .get(StarlingConstants.StorageKey.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SessionError.SESSION_EXPIRED.exception()));

        return token.toAuthorizeHeader();
    }

    private static String toFormattedDate(final Date date) {
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(date);
    }
}
