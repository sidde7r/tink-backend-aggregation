package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import com.google.api.client.http.HttpStatusCodes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.Errors;
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

public final class BelfiusApiClient {

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
            } else if (isServiceUnavailable(e)) {
                throw BankServiceError.NO_BANK_SERVICE.exception();
            }
            throw e;
        }
    }

    private boolean isAccountNotSupportedError(HttpResponseException ex) {
        HttpResponse response = ex.getResponse();
        ErrorResponse body = response.getBody(ErrorResponse.class);
        return response.getStatus() == HttpStatusCodes.STATUS_CODE_FORBIDDEN
                && (ErrorCodes.ACCOUNT_NOT_SUPPORTED.equals(body.getErrorCode())
                        || ErrorCodes.NOT_SUPPORTED.equals(body.getErrorCode()));
    }

    private boolean isServiceUnavailable(HttpResponseException ex) {
        HttpResponse response = ex.getResponse();
        ErrorResponse body = response.getBody(ErrorResponse.class);
        return response.getStatus() == HttpStatusCodes.STATUS_CODE_SERVICE_UNAVAILABLE
                && (Errors.SERVICE_UNAVAILABLE.equals(body.getError()));
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
                    && ErrorCodes.NOT_SUPPORTED.equals(responseBody.getError())) {
                throw new AccountRefreshException(responseBody.getErrorDescription());
            }
            throw e;
        }
    }

    public FetchTransactionsResponse fetchTransactionsForAccount(
            OAuth2Token oAuth2Token, String key, String logicalId) {

        URL url =
                new URL(Urls.FETCH_TRANSACTIONS_PATH)
                        .parameter(StorageKeys.LOGICAL_ID, logicalId)
                        .queryParam(QueryKeys.NEXT, key);

        HttpResponse httpResponse =
                createRequestInSession(url)
                        .header(HeaderKeys.ACCEPT, HeaderValues.TRANSACTION_ACCEPT)
                        .addBearerToken(oAuth2Token)
                        .get(HttpResponse.class);

        return SerializationUtils.deserializeFromString(
                JsonUtils.escapeNotSpecialSingleBackslashes(httpResponse.getBody(String.class)),
                FetchTransactionsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactionsForAccount(
            OAuth2Token oAuth2Token, String logicalId, String scaToken, LocalDate dateFrom) {

        URL url =
                new URL(Urls.FETCH_TRANSACTIONS_PATH).parameter(StorageKeys.LOGICAL_ID, logicalId);

        if (StringUtils.isNotBlank(scaToken)) {
            url = enrichWithDateFrom(url, dateFrom);
        }

        HttpResponse httpResponse =
                createRequestInSession(url)
                        .header(HeaderKeys.ACCEPT, HeaderValues.TRANSACTION_ACCEPT)
                        .header(HeaderValues.SCA_TOKEN, scaToken)
                        .addBearerToken(oAuth2Token)
                        .get(HttpResponse.class);

        return SerializationUtils.deserializeFromString(
                JsonUtils.escapeNotSpecialSingleBackslashes(httpResponse.getBody(String.class)),
                FetchTransactionsResponse.class);
    }

    public URL enrichWithDateFrom(URL urlToEnrich, LocalDate localDate) {
        final String formattedEarliestDate =
                localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return urlToEnrich.queryParam(QueryKeys.FROM_DATE, formattedEarliestDate);
    }
}
