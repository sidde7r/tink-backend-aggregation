package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro;

import static se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.errorhandling.ApiErrorHandler.RequestType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroConstants.URLs;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.ExchangeAuthorizationCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.configuration.AbnAmroConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.errorhandling.ApiErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.utils.AbnAmroUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class AbnAmroApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    @Getter @Setter private AbnAmroConfiguration configuration;

    public TokenResponse exchangeAuthorizationCode(final ExchangeAuthorizationCodeRequest request) {
        return post(request);
    }

    public TokenResponse refreshAccessToken(final RefreshTokenRequest request) {
        return post(request);
    }

    private TokenResponse post(final AbstractForm request) {
        RequestBuilder requestBuilder =
                client.request(URLs.OAUTH2_TOKEN_ABNAMRO)
                        .body(request, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        return ApiErrorHandler.callWithErrorHandling(
                requestBuilder, TokenResponse.class, RequestType.POST);
    }

    public ConsentResponse consentRequest() {
        RequestBuilder requestBuilder = buildRequestWithTokenAndApiKey(URLs.ABNAMRO_CONSENT_INFO);
        return ApiErrorHandler.callWithErrorHandling(
                requestBuilder, ConsentResponse.class, RequestType.GET);
    }

    public AccountHolderResponse fetchAccountHolder(String accountId) {
        RequestBuilder requestBuilder =
                buildRequestWithTokenAndApiKey(URLs.buildAccountHolderUrl(accountId));

        return ApiErrorHandler.callWithErrorHandling(
                requestBuilder, AccountHolderResponse.class, RequestType.GET);
    }

    public AccountBalanceResponse fetchAccountBalance(String accountId) {
        RequestBuilder requestBuilder =
                buildRequestWithTokenAndApiKey(URLs.buildBalanceUrl(accountId));

        return ApiErrorHandler.callWithErrorHandling(
                requestBuilder, AccountBalanceResponse.class, RequestType.GET);
    }

    public TransactionsResponse fetchTransactionsByDate(
            String accountId, LocalDate from, LocalDate to) {
        final DateTimeFormatter dtf =
                DateTimeFormatter.ofPattern(AbnAmroConstants.TRANSACTION_BOOKING_DATE_FORMAT);

        RequestBuilder requestBuilder =
                buildRequestWithTokenAndApiKey(URLs.buildTransactionsUrl(accountId))
                        .queryParam(QueryParams.BOOK_DATE_FROM, dtf.format(from))
                        .queryParam(QueryParams.BOOK_DATE_TO, dtf.format(to));

        return ApiErrorHandler.callWithErrorHandling(
                requestBuilder, TransactionsResponse.class, RequestType.GET);
    }

    public TransactionsResponse fetchTransactionsByKey(String nextPageKey, String accountId) {
        RequestBuilder requestBuilder =
                buildRequestWithTokenAndApiKey(URLs.buildTransactionsUrl(accountId))
                        .queryParam(QueryParams.NEXT_PAGE_KEY, nextPageKey);

        return ApiErrorHandler.callWithErrorHandling(
                requestBuilder, TransactionsResponse.class, RequestType.GET);
    }

    private RequestBuilder buildRequestWithTokenAndApiKey(final URL url) {
        final String apiKey = getConfiguration().getApiKey();
        return client.request(url)
                .addBearerToken(AbnAmroUtils.getOauthToken(persistentStorage))
                .header(AbnAmroConstants.QueryParams.API_KEY, apiKey)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
