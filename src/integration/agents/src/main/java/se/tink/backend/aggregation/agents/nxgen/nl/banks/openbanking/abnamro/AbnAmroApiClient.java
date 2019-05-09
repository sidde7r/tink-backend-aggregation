package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.rpc.ExchangeAuthorizationCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.configuration.AbnAmroConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.TransactionalAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.TransactionalTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.utils.AbnAmroUtils;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AbnAmroApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private AbnAmroConfiguration configuration;

    public AbnAmroApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public AbnAmroConfiguration getConfiguration() { return new AbnAmroConfiguration(); }

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

    public TransactionalAccountsResponse fetchAccounts() {
        return buildRequest(AbnAmroConstants.URLs.ABNAMRO_ACCOUNTS).get(TransactionalAccountsResponse.class);
    }

    public BalanceResponse getBalance(final String accountId) {
        return buildRequest(AbnAmroConstants.URLs.buildBalanceUrl(accountId)).get(BalanceResponse.class);
    }

    LocalDate today = LocalDate.now();

    public TransactionalTransactionsResponse getTransactions(
            final TransactionalAccount account, final int page) {

        final String accountId = account.getFromTemporaryStorage(AbnAmroConstants.StorageKey.RESOURCE_ID);
        return buildRequest(
                AbnAmroConstants.URLs.buildTransactionsUrl(accountId))
                .queryParam(AbnAmroConstants.QueryParams.BOOK_DATE_FROM, today.plusDays(-30).format(DateTimeFormatter.ofPattern(AbnAmroConstants.TRANSACTION_BOOKING_DATE_FORMAT)))
                .queryParam(AbnAmroConstants.QueryParams.BOOK_DATE_TO, today.format((DateTimeFormatter.ofPattern(AbnAmroConstants.TRANSACTION_BOOKING_DATE_FORMAT))))
                .get(TransactionalTransactionsResponse.class);
    }
}
