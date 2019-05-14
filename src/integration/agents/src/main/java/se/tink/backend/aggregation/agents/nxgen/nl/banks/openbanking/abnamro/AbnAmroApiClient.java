package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroConstants.URLs;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.rpc.ExchangeAuthorizationCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.configuration.AbnAmroConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.utils.AbnAmroUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class AbnAmroApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private AbnAmroConfiguration configuration;

    public AbnAmroApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public AbnAmroConfiguration getConfiguration() {
        return new AbnAmroConfiguration();
    }

    public void setConfiguration(AbnAmroConfiguration configuration) {
        this.configuration = configuration;
    }

    public TokenResponse exchangeAuthorizationCode(final ExchangeAuthorizationCodeRequest request) {
        return post(request);
    }

    public TokenResponse refreshAccessToken(final RefreshTokenRequest request) {
        return post(request);
    }

    private TokenResponse post(final AbstractForm request) {

        return client.request(AbnAmroConstants.URLs.OAUTH2_TOKEN_ABNAMRO)
                .body(request, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(TokenResponse.class);
    }

    private RequestBuilder buildRequest(final URL url) {

        final String apiKey = getConfiguration().getApiKey();
        return client.request(url)
                .addBearerToken(AbnAmroUtils.getOauthToken(persistentStorage))
                .header(AbnAmroConstants.QueryParams.API_KEY, apiKey)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public ConsentResponse consentRequest(OAuth2Token token) {

        final String apiKey = getConfiguration().getApiKey();
        return client.request(URLs.ABNAMRO_CONSENT_INFO)
            .addBearerToken(token)
            .header(AbnAmroConstants.QueryParams.API_KEY, apiKey)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .get(ConsentResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        String accountId = persistentStorage.get(StorageKey.ACCOUNT_CONSENT_ID);
        return buildRequest(AbnAmroConstants.URLs.buildAccountHolderUrl(accountId))
                .get(AccountsResponse.class);
    }

    public AccountHolderResponse fetchAccountHolder(final String accountId) {
        return buildRequest(AbnAmroConstants.URLs.buildAccountHolderUrl(accountId))
            .get(AccountHolderResponse.class);
    }

    public AccountBalanceResponse fetchAccountBalance(final String accountId) {
        return buildRequest(AbnAmroConstants.URLs.buildBalanceUrl(accountId))
                .get(AccountBalanceResponse.class);
    }

    public TransactionsResponse fetchTransactions(Date from, Date to) {

        String accountId = persistentStorage.get(StorageKey.ACCOUNT_CONSENT_ID);
        final String apiKey = getConfiguration().getApiKey();
        final SimpleDateFormat sdf =
            new SimpleDateFormat(AbnAmroConstants.TRANSACTION_BOOKING_DATE_FORMAT);

        TransactionsResponse response = client.request(AbnAmroConstants.URLs.buildTransactionsUrl(accountId))
            .queryParam(QueryParams.BOOK_DATE_FROM, sdf.format(from))
            .queryParam(QueryParams.BOOK_DATE_TO, sdf.format(to))
            .addBearerToken(AbnAmroUtils.getOauthToken(persistentStorage))
            .header(AbnAmroConstants.QueryParams.API_KEY, apiKey)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .get(TransactionsResponse.class);
        return response;
    }
}
