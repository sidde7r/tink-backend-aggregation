package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import java.util.Date;
import java.util.function.Supplier;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.BasicAuthorizationHeader;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.TokenRequestFactory;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.configuration.KnabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.time.KnabTimeProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

@RequiredArgsConstructor
public class KnabApiClient {

    private final TinkHttpClient client;

    private final RandomValueGenerator randomValues;

    private final KnabTimeProvider timeProvider;

    private final KnabStorage storage;

    private final String psuIpAddress;

    private KnabConfiguration configuration;

    private KnabAuthorizationCredentials authorizationCredentials;

    private TokenRequestFactory tokenRequestFactory;

    private String redirectUrl;

    public void applyConfiguration(KnabConfiguration configuration, String redirectUrl) {
        this.configuration = configuration;
        this.redirectUrl = redirectUrl;

        authorizationCredentials =
                new KnabAuthorizationCredentials(
                        configuration.getClientId(), configuration.getClientSecret());

        tokenRequestFactory = new TokenRequestFactory(authorizationCredentials, redirectUrl);
    }

    public OAuth2Token applicationAccessToken() {
        return client.request(Urls.TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.CACHE_CONTROL, HeaderValues.NO_CACHE_CONTROL)
                .header(
                        HeaderKeys.AUTHORIZATION,
                        new BasicAuthorizationHeader(authorizationCredentials).value())
                .post(TokenResponse.class, tokenRequestFactory.applicationAccessTokenRequest())
                .toTinkToken();
    }

    public String anonymousConsent(OAuth2Token applicationAccessToken) {
        return request(Urls.CONSENT)
                .addBearerToken(applicationAccessToken)
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                .post(
                        ConsentResponse.class,
                        ConsentRequest.builder()
                                .validUntil(timeProvider.date().plusDays(90))
                                .frequencyPerDay(4)
                                .recurringIndicator(true)
                                .combinedServiceIndicator(false)
                                .build())
                .getConsentId();
    }

    public URL anonymousConsentApprovalUrl(String scope, String state) {
        return Urls.AUTHORIZE
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.SCOPE, scope)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl);
    }

    public OAuth2Token accessToken(String code, String state) {
        return client.request(Urls.TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, tokenRequestFactory.accessTokenRequest(code, state))
                .toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) {
        return client.request(Urls.TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, tokenRequestFactory.refreshTokenRequest(refreshToken))
                .toTinkToken();
    }

    public boolean consentStatus(String consentId, OAuth2Token accessToken) {
        return request(Urls.CONSENT_STATUS.parameter("consent-id", consentId))
                .addBearerToken(accessToken)
                .get(ConsentStatusResponse.class)
                .isValid();
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(Urls.ACCOUNTS)
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    public BalancesResponse fetchAccountBalance(String accountId) {
        return createRequestInSession(Urls.BALANCES.parameter(PathVariables.ACCOUNT_ID, accountId))
                .get(BalancesResponse.class);
    }

    public PaginatorResponse fetchTransactions(String accountId, Date fromDate, Date toDate) {
        return createRequestInSession(
                        Urls.TRANSACTIONS.parameter(PathVariables.ACCOUNT_ID, accountId))
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(TransactionsResponse.class);
    }

    private RequestBuilder request(URL url) {
        return client.request(url)
                .header(HeaderKeys.X_REQUEST_ID, randomValues.getUUID())
                .header(HeaderKeys.DATE, timeProvider.formatted())
                .header(HeaderKeys.PSU_IP_ADDRESS, psuIpAddress)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return request(url)
                .addBearerToken(storage.findBearerToken().orElseThrow(sessionExceptionSupplier()))
                .header(
                        HeaderKeys.CONSENT_ID,
                        storage.findConsentId().orElseThrow(sessionExceptionSupplier()));
    }

    private Supplier<IllegalStateException> sessionExceptionSupplier() {
        return () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception());
    }

    static class HeaderKeys {

        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String DATE = "Date";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String AUTHORIZATION = "Authorization";
        public static final String CACHE_CONTROL = "cache-control";
    }

    static class HeaderValues {

        public static final String NO_CACHE_CONTROL = "no-cache";
    }

    static class Urls {

        public static final String BASE_AUTH_URL = "https://login.knab.nl";
        public static final String BASE_API_URL = "https://tpp-loket.knab.nl";

        public static final URL AUTHORIZE = new URL(BASE_AUTH_URL + Endpoints.AUTHORIZE);
        public static final URL TOKEN = new URL(BASE_AUTH_URL + Endpoints.TOKEN);
        public static final URL CONSENT = new URL(BASE_API_URL + Endpoints.CONSENT);
        public static final URL CONSENT_STATUS = new URL(CONSENT + "/{consent-id}/status");
        public static final URL ACCOUNTS = new URL(BASE_API_URL + Endpoints.ACCOUNTS);
        public static final URL BALANCES = new URL(BASE_API_URL + Endpoints.BALANCES);
        public static final URL TRANSACTIONS = new URL(BASE_API_URL + Endpoints.TRANSACTIONS);
    }

    static class Endpoints {
        public static final String AUTHORIZE = "/connect/authorize";
        public static final String TOKEN = "/connect/token";
        public static final String CONSENT = "/openbanking/v2/consents";
        public static final String ACCOUNTS = "/openbanking/v1/accounts";
        public static final String BALANCES = "/openbanking/v1/accounts/{accountId}/balances";
        public static final String TRANSACTIONS =
                "/openbanking/v1/accounts/{accountId}/transactions";
    }

    static class PathVariables {
        public static final String ACCOUNT_ID = "accountId";
    }

    static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String STATE = "state";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    static class QueryValues {
        public static final String CODE = "code";
        public static final String BOOKED = "booked";
    }
}
