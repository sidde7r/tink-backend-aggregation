package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.apiclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexGrantType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexHttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.configuration.AmexConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.RevokeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.RevokeResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.macgenerator.AmexMacGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

@RequiredArgsConstructor
@Slf4j
public class AmexApiClient {

    private static final String SCOPE_LIST_FOR_AUTHORIZE =
            "MEMBER_ACCT_INFO,FINS_STP_DTLS,FINS_BAL_INFO,FINS_TXN_INFO";
    private static final String SCOPE_LIST_FOR_GET_TOKEN =
            SCOPE_LIST_FOR_AUTHORIZE.replace(',', ' ');
    private static final String RETRIEVE_TOKEN_PATH = "/apiplatform/v2/oauth/token/mac";
    private static final String REFRESH_TOKEN_PATH = "/apiplatform/v1/oauth/token/refresh/mac";
    private static final String REVOKE_TOKEN_PATH = "/apiplatform/v2/oauth/token_revocation/mac";
    private static final String BASE_PATH = "/servicing/v1";

    private static final String ENDPOINT_ACCOUNTS = BASE_PATH + "/member/accounts";
    private static final String ENDPOINT_BALANCES = BASE_PATH + "/financials/balances";
    private static final String ENDPOINT_TRANSACTIONS = BASE_PATH + "/financials/transactions";

    private static final int TRANSACTION_TO_FETCH = 1000;

    private static final String QUERY_PARAM_START_DATE = "start_date";
    private static final String QUERY_PARAM_END_DATE = "end_date";
    private static final String QUERY_PARAM_LIMIT = "limit";

    private final AmexConfiguration amexConfiguration;
    private final TinkHttpClient httpClient;
    private final AmexMacGenerator amexMacGenerator;
    private final ObjectMapper objectMapper;

    URL getAuthorizeUrl(String state) {
        return httpClient
                .request(amexConfiguration.getGrantAccessJourneyUrl())
                .queryParam("redirect_uri", amexConfiguration.getRedirectUrl())
                .queryParam("client_id", amexConfiguration.getClientId())
                .queryParam("scope_list", SCOPE_LIST_FOR_AUTHORIZE)
                .queryParam("state", state)
                .getUrl();
    }

    TokenResponseDto retrieveAccessToken(String authorizationCode) {
        final TokenRequest tokenRequest =
                TokenRequest.builder()
                        .scope(SCOPE_LIST_FOR_GET_TOKEN)
                        .code(authorizationCode)
                        .redirectUri(amexConfiguration.getRedirectUrl())
                        .build();

        return httpClient
                .request(createUrl(RETRIEVE_TOKEN_PATH))
                .body(tokenRequest, MediaType.APPLICATION_FORM_URLENCODED)
                .header(AmexHttpHeaders.X_AMEX_API_KEY, amexConfiguration.getClientId())
                .header(
                        AmexHttpHeaders.AUTHENTICATION,
                        amexMacGenerator.generateAuthMacValue(AmexGrantType.AUTHORIZATION_CODE))
                .post(TokenResponseDto.class);
    }

    Optional<TokenResponseDto> refreshAccessToken(String refreshToken) {
        final RefreshRequest refreshRequest = new RefreshRequest(refreshToken);
        try {
            final TokenResponseDto response =
                    httpClient
                            .request(createUrl(REFRESH_TOKEN_PATH))
                            .body(refreshRequest, MediaType.APPLICATION_FORM_URLENCODED)
                            .header(AmexHttpHeaders.X_AMEX_API_KEY, amexConfiguration.getClientId())
                            .header(
                                    AmexHttpHeaders.AUTHENTICATION,
                                    amexMacGenerator.generateAuthMacValue(
                                            AmexGrantType.REFRESH_TOKEN))
                            .post(TokenResponseDto.class);

            return Optional.ofNullable(response);
        } catch (HttpResponseException ex) {
            log.error("Refresh token failed.");
            log.error(ex.getMessage(), ex);
        }

        return Optional.empty();
    }

    RevokeResponseDto revokeAccessToken(String accessToken) {
        final RevokeRequest revokeRequest = new RevokeRequest(accessToken);

        return httpClient
                .request(createUrl(REVOKE_TOKEN_PATH))
                .body(revokeRequest, MediaType.APPLICATION_FORM_URLENCODED)
                .header(AmexHttpHeaders.X_AMEX_API_KEY, amexConfiguration.getClientId())
                .header(
                        AmexHttpHeaders.AUTHENTICATION,
                        amexMacGenerator.generateAuthMacValue(AmexGrantType.REVOKE))
                .post(RevokeResponseDto.class);
    }

    public AccountsResponseDto fetchAccounts(HmacToken hmacToken) {
        return sendRequestAndGetResponse(ENDPOINT_ACCOUNTS, hmacToken, AccountsResponseDto.class);
    }

    @SuppressWarnings("unchecked")
    public List<BalanceDto> fetchBalances(HmacToken hmacToken) {
        final List<LinkedHashMap<String, String>> objects =
                sendRequestAndGetResponse(ENDPOINT_BALANCES, hmacToken, List.class);

        return objectMapper.convertValue(objects, new TypeReference<List<BalanceDto>>() {});
    }

    /*
     * The API only provides transaction fetching for the past 90 days. The fetcher will try to
     * catch all transactions in one call. The response when calling the transaction-endpoint will
     * contain the transactions together with an integer of how many transaction where found in the
     * 90 day period = total_transaction_count. If total_transaction_count > 1000 then we have
     * fetched to few transaction and will make a new call to fetch all transaction during our 90
     * day period. This solution were selected since the API wont work properly with pagination.
     */
    @SuppressWarnings("unchecked")
    public List<TransactionsResponseDto> fetchTransactions(HmacToken hmacToken) {

        List<TransactionsResponseDto> list =
                fetchTransactionsWithGivenLimit(hmacToken, TRANSACTION_TO_FETCH);

        int transactionCount = list.stream().findFirst().get().getTotalTransactionCount();

        // If we had more transaction than our limit during the past 90 days the we will make a new
        // call and
        // fetch all transactions.
        if (transactionCount > TRANSACTION_TO_FETCH) {
            list = fetchTransactionsWithGivenLimit(hmacToken, transactionCount);
        }

        return list;
    }

    private List<TransactionsResponseDto> fetchTransactionsWithGivenLimit(
            HmacToken hmacToken, int limit) {
        String url =
                new URL(ENDPOINT_TRANSACTIONS)
                        .queryParam(QUERY_PARAM_START_DATE, getDate90DaysBack())
                        .queryParam(
                                QUERY_PARAM_END_DATE,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()))
                        .queryParam(QUERY_PARAM_LIMIT, Integer.toString(limit))
                        .toString();

        List<LinkedHashMap<String, String>> objects =
                sendRequestAndGetResponse(url, hmacToken, List.class);

        List<TransactionsResponseDto> list =
                objectMapper.convertValue(
                        objects, new TypeReference<List<TransactionsResponseDto>>() {});

        return list;
    }

    private String getDate90DaysBack() {
        return LocalDate.now().minusDays(Period.ofDays(90).getDays()).toString();
    }

    private <T> T sendRequestAndGetResponse(
            String resourcePath, HmacToken hmacToken, Class<T> clazz) {
        return httpClient
                .request(createUrl(resourcePath))
                .header(AmexHttpHeaders.X_AMEX_API_KEY, amexConfiguration.getClientId())
                .header(
                        HttpHeaders.AUTHORIZATION,
                        amexMacGenerator.generateDataMacValue(
                                UriBuilder.fromUri(resourcePath)
                                        .replaceQuery(null)
                                        .build()
                                        .toString(),
                                hmacToken))
                .header(
                        AmexHttpHeaders.X_AMEX_REQUEST_ID,
                        UUID.randomUUID().toString().replace("-", ""))
                .get(clazz);
    }

    private URL createUrl(String path) {
        return new URL(String.format("%s%s", amexConfiguration.getServerUrl(), path));
    }
}
