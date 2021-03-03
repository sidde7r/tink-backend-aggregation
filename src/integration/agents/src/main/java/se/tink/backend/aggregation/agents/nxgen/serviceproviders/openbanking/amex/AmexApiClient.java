package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.configuration.AmexConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.StatementPeriodsDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.RevokeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.RevokeResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.macgenerator.AmexMacGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac.HmacMultiTokenStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

@RequiredArgsConstructor
@Slf4j
public class AmexApiClient {

    private final AmexConfiguration amexConfiguration;
    private final String redirectUrl;
    private final TinkHttpClient httpClient;
    private final AmexMacGenerator amexMacGenerator;
    private final ObjectMapper objectMapper;
    private final TemporaryStorage temporaryStorage;
    private final HmacMultiTokenStorage hmacMultiTokenStorage;
    private boolean logout;

    public URL getAuthorizeUrl(String state) {
        return Urls.GRANT_ACCESS_JOURNEY_URL
                .queryParam(AmericanExpressConstants.QueryParams.REDIRECT_URI, redirectUrl)
                .queryParam(
                        AmericanExpressConstants.QueryParams.CLIENT_ID,
                        amexConfiguration.getClientId())
                .queryParam(
                        AmericanExpressConstants.QueryParams.SCOPE_LIST,
                        AmericanExpressConstants.QueryValues.SCOPE_LIST_FOR_AUTHORIZE)
                .queryParam(AmericanExpressConstants.QueryParams.STATE, state);
    }

    public TokenResponseDto retrieveAccessToken(String authorizationCode) {
        final TokenRequest tokenRequest =
                TokenRequest.builder()
                        .scope(AmericanExpressConstants.QueryValues.SCOPE_LIST_FOR_GET_TOKEN)
                        .code(authorizationCode)
                        .redirectUri(redirectUrl)
                        .build();

        return httpClient
                .request(Urls.RETRIEVE_TOKEN_PATH)
                .body(tokenRequest, MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        AmericanExpressConstants.Headers.X_AMEX_API_KEY,
                        amexConfiguration.getClientId())
                .header(
                        AmericanExpressConstants.Headers.AUTHENTICATION,
                        amexMacGenerator.generateAuthMacValue(AmexGrantType.AUTHORIZATION_CODE))
                .post(TokenResponseDto.class);
    }

    public Optional<TokenResponseDto> refreshAccessToken(String refreshToken) {
        final RefreshRequest refreshRequest = new RefreshRequest(refreshToken);
        try {
            final TokenResponseDto response =
                    httpClient
                            .request(Urls.REFRESH_TOKEN_PATH)
                            .body(refreshRequest, MediaType.APPLICATION_FORM_URLENCODED)
                            .header(
                                    AmericanExpressConstants.Headers.X_AMEX_API_KEY,
                                    amexConfiguration.getClientId())
                            .header(
                                    AmericanExpressConstants.Headers.AUTHENTICATION,
                                    amexMacGenerator.generateAuthMacValue(
                                            AmexGrantType.REFRESH_TOKEN))
                            .post(TokenResponseDto.class);

            return Optional.ofNullable(response);
        } catch (SessionException ex) {
            log.error("Refresh token failed.");
            log.error(ex.getMessage(), ex);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                log.info("Refresh token unsuccessful: {}", e.getMessage());
            } else {
                throw e;
            }
        }

        return Optional.empty();
    }

    public RevokeResponseDto revokeAccessToken(String accessToken) {
        final RevokeRequest revokeRequest = new RevokeRequest(accessToken);

        return httpClient
                .request(Urls.REVOKE_TOKEN_PATH)
                .body(revokeRequest, MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        AmericanExpressConstants.Headers.X_AMEX_API_KEY,
                        amexConfiguration.getClientId())
                .header(
                        AmericanExpressConstants.Headers.AUTHENTICATION,
                        amexMacGenerator.generateAuthMacValue(AmexGrantType.REVOKE))
                .post(RevokeResponseDto.class);
    }

    public void revokeAccessToken() {
        String token =
                hmacMultiTokenStorage
                        .getToken()
                        .flatMap(t -> t.getTokens().stream().findFirst())
                        .get()
                        .getAccessToken();

        revokeAccessToken(token);
        hmacMultiTokenStorage.clearToken();
        log.info("Access token revoked and cleared from storage");
    }

    public AccountsResponseDto fetchAccounts(HmacToken hmacToken) {
        return sendRequestAndGetResponse(
                AmericanExpressConstants.Urls.ENDPOINT_ACCOUNTS,
                hmacToken,
                AccountsResponseDto.class);
    }

    public StatementPeriodsDto fetchStatementPeriods(HmacToken hmacToken) {
        return sendRequestAndGetResponse(
                Urls.ENDPOINT_STATEMENT_PERIODS, hmacToken, StatementPeriodsDto.class);
    }

    @SuppressWarnings("unchecked")
    public List<BalanceDto> fetchBalances(HmacToken hmacToken) {
        final List<LinkedHashMap<String, String>> objects =
                sendRequestAndGetResponse(
                        AmericanExpressConstants.Urls.ENDPOINT_BALANCES, hmacToken, List.class);

        return objectMapper.convertValue(objects, new TypeReference<List<BalanceDto>>() {});
    }

    /*
     * The API provides an option to fetch posted transactions for more than 90 days based on statement periods.
     * First call is to fetch any pending transactions for last 30 days from request date. Subsequently after,
     * the function will fetch posted transactions based on the statement periods data.
     */
    public List<TransactionsResponseDto> fetchTransactions(
            HmacToken hmacToken, Optional<LocalDate> fromDate, LocalDate toDate) {

        List<TransactionsResponseDto> transactionResponses =
                fetchTransactionsWithGivenLimit(
                        hmacToken,
                        AmericanExpressConstants.QueryValues.TRANSACTION_TO_FETCH,
                        fromDate,
                        toDate);

        int transactionCount =
                transactionResponses.stream()
                        .map(TransactionsResponseDto::getTotalTransactionCount)
                        .mapToInt(Integer::intValue)
                        .sum();

        /*
         *If we have more transaction than our limit (currently set to 1000)
         * then we will make a new call and fetch all transactions.
         */
        if (transactionCount > AmericanExpressConstants.QueryValues.TRANSACTION_TO_FETCH) {
            transactionResponses =
                    fetchTransactionsWithGivenLimit(hmacToken, transactionCount, fromDate, toDate);
        }

        return transactionResponses;
    }

    private URL buildTransactionsUrl(int limit, Optional<LocalDate> fromDate, LocalDate toDate) {
        URL urlWithBaseQueryParams =
                Urls.ENDPOINT_TRANSACTIONS
                        .queryParam(
                                QueryParams.EXTENDED_DETAILS,
                                AmericanExpressConstants.QueryValues.MERCHANT)
                        .queryParam(
                                AmericanExpressConstants.QueryParams.QUERY_PARAM_LIMIT,
                                Integer.toString(limit));

        // Pending transactions
        if (fromDate.isPresent()) {
            return urlWithBaseQueryParams
                    .queryParam(
                            AmericanExpressConstants.QueryParams.QUERY_PARAM_START_DATE,
                            fromDate.get().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .queryParam(
                            AmericanExpressConstants.QueryParams.QUERY_PARAM_END_DATE,
                            toDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .queryParam(
                            AmericanExpressConstants.QueryParams.STATUS,
                            AmericanExpressConstants.QueryValues.PENDING);
        }

        // Posted transactions, from documentation "if status is not specified, this API returns
        // posted transactions by default."
        return urlWithBaseQueryParams.queryParam(
                AmericanExpressConstants.QueryParams.QUERY_PARAM_STATEMENT_END_DATE,
                toDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /*
     * Will fetch posted and pending transactions in two separate Api-calls. The resulting lists will then be merged.
     */
    private List<TransactionsResponseDto> fetchTransactionsWithGivenLimit(
            HmacToken hmacToken, int limit, Optional<LocalDate> fromDate, LocalDate toDate) {

        URL url;
        List<LinkedHashMap<String, String>> transactions;

        url = buildTransactionsUrl(limit, fromDate, toDate);

        transactions = finalizeAndSendRequest(url, hmacToken);

        // only store transactions from statement periods and used for account-specific mapping.
        if (!fromDate.isPresent()) {
            temporaryStorage.put(
                    AmericanExpressUtils.createAndGetStorageString(
                            toDate.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                    transactions);
        }
        /* this workaround is necessary since the response from Amex cannot be directly mapped to a
         TransactionResponseDto due to the response is not in a correct format.
        */
        return objectMapper.convertValue(
                transactions, new TypeReference<List<TransactionsResponseDto>>() {});
    }

    private List<LinkedHashMap<String, String>> finalizeAndSendRequest(
            URL url, HmacToken hmacToken) {
        return sendRequestAndGetResponse(url, hmacToken, List.class);
    }

    private <T> T sendRequestAndGetResponse(URL url, HmacToken hmacToken, Class<T> clazz) {
        return httpClient
                .request(url)
                .header(
                        AmericanExpressConstants.Headers.X_AMEX_API_KEY,
                        amexConfiguration.getClientId())
                .header(
                        HttpHeaders.AUTHORIZATION,
                        amexMacGenerator.generateDataMacValue(url.toUri().getPath(), hmacToken))
                .header(
                        AmericanExpressConstants.Headers.X_AMEX_REQUEST_ID,
                        UUID.randomUUID().toString().replace("-", ""))
                .get(clazz);
    }

    public boolean shouldLogout() {
        return logout;
    }

    public void setLogout(boolean logout) {
        this.logout = logout;
    }
}
