package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.cookie.Cookie;
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
                        .header(AmericanExpressV62Constants.ConstantValueHeaders.CHARSET)
                        .header(AmericanExpressV62Constants.ConstantValueHeaders.CLIENT_TYPE)
                        .header(AmericanExpressV62Constants.ConstantValueHeaders.DEVICE_MODEL)
                        .header(AmericanExpressV62Constants.ConstantValueHeaders.DEVICE_OS)
                        .header(AmericanExpressV62Constants.ConstantValueHeaders.OS_VERSION)
                        .header(AmericanExpressV62Constants.ConstantValueHeaders.MANUFACTURER)
                        .header(AmericanExpressV62Constants.ConstantValueHeaders.TIMEZONE_NAME)
                        .header(AmericanExpressV62Constants.ConstantValueHeaders.TIMEZONE_OFFSET)
                        .header(AmericanExpressV62Constants.ConstantValueHeaders.ACCEPT_ENCODING)
                        .header(AmericanExpressV62Constants.ConstantValueHeaders.ACCEPT_LANGUAGE)
                        .header(HttpHeaders.USER_AGENT, config.getUserAgent())
                        .header(
                                AmericanExpressV62Constants.Headers.REQUEST_ID,
                                UUID.randomUUID().toString().toUpperCase())
                        .header(
                                AmericanExpressV62Constants.Headers.INSTALLATION_ID,
                                persistentStorage.get(
                                        AmericanExpressV62Constants.Tags.INSTALLATION_ID))
                        .header(
                                AmericanExpressV62Constants.Headers.HARDWARE_ID,
                                persistentStorage.get(AmericanExpressV62Constants.Tags.HARDWARE_ID))
                        .header(
                                AmericanExpressV62Constants.Headers.PROCESS_ID,
                                persistentStorage.get(AmericanExpressV62Constants.Tags.PROCESS_ID))
                        .header(
                                AmericanExpressV62Constants.Headers.PUBLIC_GUID,
                                persistentStorage.getOrDefault(
                                        AmericanExpressV62Constants.Tags.PUBLIC_GUID,
                                        AmericanExpressV62Constants.HeadersValue.UNAVAILABLE))
                        .header(AmericanExpressV62Constants.Headers.APP_ID, config.getAppId())
                        .header(
                                AmericanExpressV62Constants.Headers.APP_VERSION,
                                config.getAppVersion())
                        .header(AmericanExpressV62Constants.Headers.LOCALE, config.getLocale());

        if (config.getGitSha() != null) {
            requestBuilder.header(AmericanExpressV62Constants.Headers.GIT_SHA, config.getGitSha());
        }

        return requestBuilder;
    }

    protected RequestBuilder createRequestInSession(String uri) {
        return createRequest(uri)
                .header(
                        AmericanExpressV62Constants.Headers.SESSION,
                        sessionStorage.get(AmericanExpressV62Constants.Tags.SESSION_ID))
                .header(
                        AmericanExpressV62Constants.Headers.CUPCAKE,
                        sessionStorage.get(AmericanExpressV62Constants.Tags.CUPCAKE))
                .header(
                        AmericanExpressV62Constants.Headers.GATEKEEPER,
                        sessionStorage.get(AmericanExpressV62Constants.Tags.GATEKEEPER))
                .header(
                        AmericanExpressV62Constants.Headers.AUTHORIZATION,
                        sessionStorage.get(AmericanExpressV62Constants.Tags.AUTHORIZATION));
    }

    public InitializationResponse initialization(InitializationRequest request) {
        return createRequest(Urls.INITIALIZATION)
                .header(AmericanExpressV62Constants.Headers.REQUEST_SEQUENCE, 0)
                .post(InitializationResponse.class, request);
    }

    public LogonResponse logon(LogonRequest request) {
        String rawResponse =
                createRequest(AmericanExpressV62Constants.Urls.LOG_ON)
                        .header(AmericanExpressV62Constants.Headers.REQUEST_SEQUENCE, 1)
                        .post(String.class, request);

        return AmericanExpressV62Utils.fromJson(rawResponse, LogonResponse.class);
    }

    public TimelineResponse requestTimeline(TimelineRequest request) {
        return createRequestInSession(AmericanExpressV62Constants.Urls.TIMELINE)
                .post(TimelineResponse.class, request);
    }

    public TransactionResponse requestTransaction(TransactionsRequest request) {

        return createRequestInSession(AmericanExpressV62Constants.Urls.TRANSACTION)
                .post(TransactionResponse.class, request);
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
