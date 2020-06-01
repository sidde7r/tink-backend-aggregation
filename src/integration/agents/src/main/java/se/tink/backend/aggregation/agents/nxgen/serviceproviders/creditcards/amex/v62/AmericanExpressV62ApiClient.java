package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.ConstantValueHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Headers;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.HeadersValue;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.PATTERN;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Tags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.InitializationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.InitializationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.KeyExchangeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.KeyExchangeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils.AmericanExpressV62Utils;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AmericanExpressV62ApiClient {
    private static final String AGENT_ID_COOKIE = "agent-id";

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final AmericanExpressV62Configuration config;

    public AmericanExpressV62ApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            AmericanExpressV62Configuration config) {
        this.client = client;
        client.disableAggregatorHeader();
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.config = config;
    }

    protected RequestBuilder createRequest(String uri) {
        final URL url = new URL(AmericanExpressV62Constants.BASE_API + uri);
        final String date = PATTERN.DATE_FORMATTER.format(new Date());

        return client.request(url)
                .header(ConstantValueHeaders.ACCEPT_JSON)
                .header(ConstantValueHeaders.CONTENT_TYPE_JSON)
                .header(ConstantValueHeaders.AUTHORITY)
                .header(ConstantValueHeaders.CLIENT_TYPE)
                .header(ConstantValueHeaders.CHARSET)
                .header(ConstantValueHeaders.DEVICE_MODEL)
                .header(ConstantValueHeaders.DEVICE_OS)
                .header(ConstantValueHeaders.OS_VERSION)
                .header(ConstantValueHeaders.MANUFACTURER)
                .header(ConstantValueHeaders.TIMEZONE_NAME)
                .header(ConstantValueHeaders.TIMEZONE_OFFSET)
                .header(ConstantValueHeaders.ACCEPT_ENCODING)
                .header(ConstantValueHeaders.ACCEPT_LANGUAGE)
                .header(Headers.USER_AGENT, config.getUserAgent())
                .header(Headers.REQUEST_ID, UUID.randomUUID().toString().toUpperCase())
                .header(Headers.INSTALLATION_ID, persistentStorage.get(Tags.INSTALLATION_ID))
                .header(Headers.HARDWARE_ID, persistentStorage.get(Tags.HARDWARE_ID))
                .header(Headers.PROCESS_ID, persistentStorage.get(Tags.PROCESS_ID))
                .header(
                        Headers.PUBLIC_GUID,
                        persistentStorage.getOrDefault(Tags.PUBLIC_GUID, HeadersValue.UNAVAILABLE))
                .header(Headers.APP_ID, config.getAppId())
                .header(Headers.APP_VERSION, config.getAppVersion())
                .header(Headers.LOCALE, config.getLocale())
                .header(Headers.DEVICE_TIME, date)
                .header(Headers.GIT_SHA, calculateGitSha());
    }

    private String calculateGitSha() {
        if ("6.29.0".equalsIgnoreCase(config.getAppVersion())) {
            return calculateGitSha(HeadersValue.START_DATE_V29, HeadersValue.COMMIT_HASH_V29);
        } else if ("6.30.0".equalsIgnoreCase(config.getAppVersion())) {
            return calculateGitSha(HeadersValue.START_DATE_V30, HeadersValue.COMMIT_HASH_V30);
        }
        throw new IllegalStateException("Undefined app version");
    }

    private String calculateGitSha(String startDate, String commitHash) {
        final int salt = calculateHashSalt(startDate);
        final String data = String.format("%s|%s|%d", config.getAppId(), commitHash, salt);
        return Hash.sha256AsHex(data.getBytes()).substring(0, 9);
    }

    // The hash salt is equal to the total hours from start date divided by 11 rounded down.
    private int calculateHashSalt(String startDateAsString) {
        final SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        Date start;
        try {
            start = format.parse(startDateAsString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format", e);
        }
        final Date now = new Date();
        assert start != null;
        final BigDecimal diff =
                BigDecimal.valueOf(now.getTime()).subtract(BigDecimal.valueOf(start.getTime()));
        final BigDecimal diffHours =
                diff.divide(BigDecimal.valueOf(60 * 60 * 1000), RoundingMode.FLOOR);
        return diffHours.divideToIntegralValue(BigDecimal.valueOf(11)).intValue();
    }

    protected RequestBuilder createRequestInSession(String uri) {
        return createRequest(uri)
                .header(Headers.SESSION, sessionStorage.get(Tags.SESSION_ID))
                .header(Headers.CUPCAKE, sessionStorage.get(Tags.CUPCAKE))
                .header(Headers.GATEKEEPER, sessionStorage.get(Tags.GATEKEEPER))
                .header(Headers.AUTHORIZATION, sessionStorage.get(Tags.AUTHORIZATION));
    }

    public KeyExchangeResponse keyExchange(KeyExchangeRequest keyExchangeRequest) {
        return client.request(AmericanExpressV62Constants.KEY_EXCHANGE_URL)
                .header(
                        Headers.TRACKING_ID,
                        UUID.randomUUID().toString().replaceAll("-", "").toUpperCase())
                .accept(MediaType.WILDCARD_TYPE)
                .header(
                        HttpHeaders.ACCEPT_ENCODING,
                        ConstantValueHeaders.ACCEPT_ENCODING.getValue())
                .header(
                        HttpHeaders.ACCEPT_LANGUAGE,
                        ConstantValueHeaders.ACCEPT_LANGUAGE.getValue())
                .header(HttpHeaders.CONTENT_TYPE, ConstantValueHeaders.CONTENT_TYPE_JSON.getValue())
                .header(Headers.APP_BLOCK, HeadersValue.APP_BLOCK)
                .header(HttpHeaders.USER_AGENT, config.getUserAgent())
                .header(Headers.CLIENT_ID, HeadersValue.CLIENT_ID)
                .post(KeyExchangeResponse.class, keyExchangeRequest);
    }

    // Purpose of this api call is to retrieve a cookie named "SaneId"
    public void fetchSaneIdCookie() {
        client.request(AmericanExpressV62Constants.BASE_API + Urls.SANE_ID)
                .queryParam(QueryKeys.FACE, config.getLocale())
                .queryParam(QueryKeys.CLIENT_TYPE, QueryValues.CLIENT_TYPE_VALUE)
                .queryParam(QueryKeys.PAGE, QueryValues.PAGE_VALUE)
                .queryParam(QueryKeys.VERSION, QueryValues.VERSION_VALUE)
                .header(ConstantValueHeaders.AUTHORITY)
                .header(ConstantValueHeaders.USER_AGENT)
                .header(ConstantValueHeaders.ACCEPT_TEXT)
                .header(ConstantValueHeaders.ACCEPT_LANGUAGE)
                .header(ConstantValueHeaders.ACCEPT_ENCODING)
                .get(String.class);
    }

    public InitializationResponse initialization() {
        final String initVersion =
                persistentStorage.getOrDefault(Tags.INIT_VERSION, config.getInitVersion());
        final InitializationRequest request =
                InitializationRequest.createAccountServicingRequest(initVersion);

        return createRequest(Urls.INITIALIZATION)
                .header(Headers.REQUEST_SEQUENCE, 0)
                .post(InitializationResponse.class, request);
    }

    public LogonResponse logon(LogonRequest request) {
        String rawResponse =
                createRequest(Urls.LOG_ON)
                        .header(Headers.REQUEST_SEQUENCE, 1)
                        .post(String.class, request);

        return AmericanExpressV62Utils.fromJson(rawResponse, LogonResponse.class);
    }

    public TimelineResponse requestTimeline(TimelineRequest request) {
        return createRequestInSession(Urls.TIMELINE).post(TimelineResponse.class, request);
    }

    public TransactionResponse requestTransaction(TransactionsRequest request) {

        return createRequestInSession(Urls.TRANSACTION).post(TransactionResponse.class, request);
    }

    // TODO: remove if will not be needed again in near future
    private String prepareLogonCookie() {
        String agentIdCookieValue =
                client.getCookies().stream()
                        .filter(cookie -> AGENT_ID_COOKIE.equals(cookie.getName()))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElse(StringUtils.EMPTY);

        client.clearCookies();
        return AGENT_ID_COOKIE + "=" + agentIdCookieValue;
    }
}
