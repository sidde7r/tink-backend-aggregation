package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.ConstantValueHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Headers;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.HeadersValue;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Tags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.InitializationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.InitializationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils.AmericanExpressV62Utils;
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
        URL url = new URL(AmericanExpressV62Constants.BASE_API + uri);
        RequestBuilder requestBuilder =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
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
                        .header(HttpHeaders.USER_AGENT, config.getUserAgent())
                        .header(Headers.REQUEST_ID, UUID.randomUUID().toString().toUpperCase())
                        .header(
                                Headers.INSTALLATION_ID,
                                persistentStorage.get(Tags.INSTALLATION_ID))
                        .header(Headers.HARDWARE_ID, persistentStorage.get(Tags.HARDWARE_ID))
                        .header(Headers.PROCESS_ID, persistentStorage.get(Tags.PROCESS_ID))
                        .header(
                                Headers.PUBLIC_GUID,
                                persistentStorage.getOrDefault(
                                        Tags.PUBLIC_GUID, HeadersValue.UNAVAILABLE))
                        .header(Headers.APP_ID, config.getAppId())
                        .header(Headers.APP_VERSION, config.getAppVersion())
                        .header(Headers.LOCALE, config.getLocale());

        if (config.getGitSha() != null) {
            requestBuilder.header(Headers.GIT_SHA, config.getGitSha());
        }

        return requestBuilder;
    }

    protected RequestBuilder createRequestInSession(String uri) {
        return createRequest(uri)
                .header(Headers.SESSION, sessionStorage.get(Tags.SESSION_ID))
                .header(Headers.CUPCAKE, sessionStorage.get(Tags.CUPCAKE))
                .header(Headers.GATEKEEPER, sessionStorage.get(Tags.GATEKEEPER))
                .header(Headers.AUTHORIZATION, sessionStorage.get(Tags.AUTHORIZATION));
    }

    // Purpose of this api call is to retrieve a cookie named "SaneId"
    public void fetchSaneIdCookie() {
        client.request(AmericanExpressV62Constants.BASE_API + Urls.SANE_ID)
                .queryParam(QueryKeys.FACE, QueryValues.FACE_VALUE)
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

    public InitializationResponse initialization(InitializationRequest request) {
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
