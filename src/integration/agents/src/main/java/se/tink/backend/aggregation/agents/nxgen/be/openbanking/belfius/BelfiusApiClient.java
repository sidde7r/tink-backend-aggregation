package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import com.google.api.client.http.HttpStatusCodes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.utils.CryptoUtils;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.json.JsonUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public final class BelfiusApiClient {

    private static final String SCA_INVALID = "Invalid JWT";

    private final TinkHttpClient client;
    private final BelfiusConfiguration configuration;
    private final String redirectUrl;
    private final RandomValueGenerator randomValueGenerator;

    public BelfiusApiClient(
            TinkHttpClient client,
            AgentConfiguration<BelfiusConfiguration> agentConfiguration,
            RandomValueGenerator randomValueGenerator) {
        this.client = client;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.randomValueGenerator = randomValueGenerator;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url).acceptLanguage(HeaderValues.ACCEPT_LANGUAGE);
    }

    private RequestBuilder createRequestInSession(URL url) {

        return createRequest(url)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.REDIRECT_URI, redirectUrl)
                .header(HeaderKeys.REQUEST_ID, randomValueGenerator.getUUID());
    }

    public List<ConsentResponse> getConsent(URL url, String iban, String code) {
        try {
            ConsentResponse[] consentResponses =
                    createRequestInSession(url)
                            .queryParam(QueryKeys.IBAN, iban)
                            .queryParam(QueryKeys.SCOPE, "AIS")
                            .header(HeaderKeys.ACCEPT, HeaderValues.CONSENT_ACCEPT)
                            .header(HeaderKeys.CODE_CHALLENGE, CryptoUtils.getCodeChallenge(code))
                            .header(
                                    HeaderKeys.CODE_CHALLENGE_METHOD,
                                    HeaderValues.CODE_CHALLENGE_TYPE)
                            .get(ConsentResponse[].class);
            return Arrays.asList(consentResponses);
        } catch (HttpResponseException e) {
            if (isAccountNotSupportedError(e)) {
                throw LoginError.NOT_SUPPORTED.exception(
                        "This account can't be consulted via electronic channel");
            }
            throw e;
        }
    }

    private boolean isAccountNotSupportedError(HttpResponseException ex) {
        HttpResponse response = ex.getResponse();
        ErrorResponse body = response.getBody(ErrorResponse.class);
        return response.getStatus() == HttpStatusCodes.STATUS_CODE_FORBIDDEN
                && (ErrorCodes.CHANNEL_NOT_PERMITTED.equalsIgnoreCase(body.getErrorCode())
                        || ErrorCodes.ACCOUNT_NOT_SUPPORTED.equalsIgnoreCase(body.getErrorCode()));
    }

    public TokenResponse postToken(URL url, String tokenEntity) {
        return createRequest(url)
                .addBasicAuth(configuration.getClientId(), configuration.getClientSecret())
                .header(HeaderKeys.ACCEPT, HeaderValues.TOKEN_ACCEPT)
                .header(HeaderKeys.REQUEST_ID, randomValueGenerator.getUUID())
                .header(HeaderKeys.CONTENT_TYPE, HeaderValues.CONTENT_TYPE)
                .body(tokenEntity, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class);
    }

    public FetchAccountResponse fetchAccountById(OAuth2Token oAuth2Token, String logicalId) {
        try {
            return createRequestInSession(new URL(Urls.FETCH_ACCOUNT_PATH + logicalId))
                    .header(HeaderKeys.ACCEPT, HeaderValues.ACCOUNT_ACCEPT)
                    .addBearerToken(oAuth2Token)
                    .get(FetchAccountResponse.class);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            ErrorResponse responseBody = response.getBody(ErrorResponse.class);
            if (response.getStatus() == HttpStatusCodes.STATUS_CODE_FORBIDDEN
                    && ErrorCodes.ACCOUNT_NOT_SUPPORTED.equalsIgnoreCase(
                            responseBody.getErrorCode())) {
                throw new AccountRefreshException(responseBody.getErrorDescription());
            }
            throw e;
        }
    }

    public FetchTransactionsResponse fetchTransactionsFromLast90Days(
            OAuth2Token oAuth2Token, String nextKey, String logicalId) {

        HttpResponse httpResponse =
                makeFetchTransactionsCall(oAuth2Token, nextKey, logicalId, null, null, null);

        return mapHttpResponseToFetchTransactionsResponse(httpResponse);
    }

    public FetchTransactionsResponse fetchTransactionsFromDate(
            OAuth2Token oAuth2Token,
            String nextKey,
            String logicalId,
            String scaToken,
            LocalDate dateFrom,
            String pageSize) {

        HttpResponse httpResponse;
        try {
            httpResponse =
                    makeFetchTransactionsCall(
                            oAuth2Token, nextKey, logicalId, scaToken, dateFrom, pageSize);
        } catch (BankServiceException e) {
            log.error(
                    "Page size: {} too large when fetching transactions for account :{} from date: {}",
                    pageSize,
                    logicalId,
                    dateFrom);
            throw e;
        } catch (HttpResponseException e) {
            HttpResponse exceptionResponse = e.getResponse();
            if (exceptionResponse.getStatus() == 401
                    && exceptionResponse.getBody(String.class).contains(SCA_INVALID)) {
                log.error(
                        "SCA token was invalid, trying to fetch transactions from 90 days without SCA token.");
            }
            throw e;
        }

        return mapHttpResponseToFetchTransactionsResponse(httpResponse);
    }

    private HttpResponse makeFetchTransactionsCall(
            OAuth2Token oAuth2Token,
            String nextKey,
            String logicalId,
            String scaToken,
            LocalDate dateFrom,
            String pageSize) {

        URL url = prepareUrlForTransactions(logicalId, nextKey, dateFrom, pageSize);

        RequestBuilder requestBuilder =
                createRequestInSession(url)
                        .header(HeaderKeys.ACCEPT, HeaderValues.TRANSACTION_ACCEPT)
                        .addBearerToken(oAuth2Token);

        if (StringUtils.isNotBlank(scaToken)) {
            requestBuilder.header(HeaderValues.SCA_TOKEN, scaToken);
        }

        return requestBuilder.get(HttpResponse.class);
    }

    private URL prepareUrlForTransactions(
            String logicalId, String nextKey, LocalDate dateFrom, String pageSize) {
        Map<String, String> queryParams = new HashMap<>();
        if (StringUtils.isNotBlank(nextKey)) {
            queryParams.put(QueryKeys.NEXT, nextKey);
        }
        if (dateFrom != null) {
            final String formattedEarliestDate =
                    dateFrom.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            queryParams.put(QueryKeys.FROM_DATE, formattedEarliestDate);
        }
        if (StringUtils.isNotBlank(pageSize)) {
            queryParams.put(QueryKeys.PAGE_SIZE, pageSize);
        }

        return new URL(Urls.FETCH_TRANSACTIONS_PATH)
                .parameter(StorageKeys.LOGICAL_ID, logicalId)
                .queryParams(queryParams);
    }

    private FetchTransactionsResponse mapHttpResponseToFetchTransactionsResponse(
            HttpResponse httpResponse) {
        return SerializationUtils.deserializeFromString(
                JsonUtils.escapeNotSpecialSingleBackslashes(httpResponse.getBody(String.class)),
                FetchTransactionsResponse.class);
    }
}
