package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.utils.CryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.utils.TimeUtils;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class BelfiusApiClient {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    private final TinkHttpClient client;
    private final BelfiusConfiguration configuration;
    private final String redirectUrl;

    public BelfiusApiClient(
            TinkHttpClient client, AgentConfiguration<BelfiusConfiguration> agentConfiguration) {
        this.client = client;
        this.configuration = agentConfiguration.getClientConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .acceptLanguage(HeaderValues.ACCEPT_LANGUAGE)
                .header(HeaderKeys.ACCEPT, HeaderValues.ACCEPT);
    }

    private RequestBuilder createRequestInSession(URL url) {

        return createRequest(url)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.REDIRECT_URI, redirectUrl)
                .header(HeaderKeys.REQUEST_ID, UUID.randomUUID());
    }

    public List<ConsentResponse> getConsent(URL url, String iban, String code) {

        ConsentResponse[] consentResponses =
                createRequestInSession(url)
                        .queryParam(QueryKeys.IBAN, iban)
                        .header(HeaderKeys.CODE_CHALLENGE, CryptoUtils.getCodeChallenge(code))
                        .header(HeaderKeys.CODE_CHALLENGE_METHOD, HeaderValues.CODE_CHALLENGE_TYPE)
                        .get(ConsentResponse[].class);

        return Arrays.asList(consentResponses);
    }

    public TokenResponse postToken(URL url, String tokenEntity) {
        return createRequest(url)
                .addBasicAuth(configuration.getClientId(), configuration.getClientSecret())
                .header(HeaderKeys.REQUEST_ID, UUID.randomUUID())
                .body(tokenEntity, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class);
    }

    public FetchAccountResponse fetchAccountById(OAuth2Token oAuth2Token, String logicalId) {

        return createRequestInSession(
                        new URL(configuration.getBaseUrl() + Urls.FETCH_ACCOUNT_PATH + logicalId))
                .addBearerToken(oAuth2Token)
                .get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactionsForAccount(
            Date toDate, OAuth2Token oAuth2Token, String logicalId) {
        final URL url =
                new URL(configuration.getBaseUrl() + Urls.FETCH_TRANSACTIONS_PATH)
                        .parameter(StorageKeys.LOGICAL_ID, logicalId);

        HttpResponse httpResponse =
                createRequestInSession(url)
                        .addBearerToken(oAuth2Token)
                        .queryParam(
                                QueryKeys.FROM_DATE,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(
                                        TimeUtils.get90DaysDate(toDate)))
                        .queryParam(
                                QueryKeys.TO_DATE,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                        .get(HttpResponse.class);

        try {
            return OBJECT_MAPPER.readValue(
                    httpResponse.getBodyInputStream(), FetchTransactionsResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String createPayment(CreatePaymentRequest body, String signature) {
        client.setFollowRedirects(false);
        HttpResponse res =
                createRequestInSession(
                                new URL(configuration.getBaseUrl().concat(Urls.CREATE_PAYMENT)))
                        .type(MediaType.APPLICATION_JSON)
                        .header(
                                HeaderKeys.CODE_CHALLENGE,
                                CryptoUtils.getCodeChallenge(CryptoUtils.getCodeVerifier()))
                        .header(HeaderKeys.CODE_CHALLENGE_METHOD, HeaderValues.CODE_CHALLENGE_TYPE)
                        .header(HeaderKeys.SIGNATURE, signature)
                        .post(HttpResponse.class, body);

        return res.getHeaders().get(HeaderKeys.LOCATION).get(0);
    }
}
