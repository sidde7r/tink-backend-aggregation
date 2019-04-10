package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.session.rpc.ExtendResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.session.rpc.LogoffResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AmericanExpressV62ApiClient {
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
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.config = config;
    }

    protected RequestBuilder createRequest(String uri) {
        URL url = new URL(AmericanExpressV62Constants.BASE_API + uri);
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(AmericanExpressV62Constants.Headers.APP_ID, config.getAppId())
                .header(HttpHeaders.USER_AGENT, config.getUserAgent())
                .header(AmericanExpressV62Constants.ConstantValueHeaders.CHARSET)
                .header(AmericanExpressV62Constants.ConstantValueHeaders.CLIENT_TYPE)
                .header(AmericanExpressV62Constants.ConstantValueHeaders.APP_VERSION)
                .header(AmericanExpressV62Constants.ConstantValueHeaders.DEVICE_MODEL)
                .header(AmericanExpressV62Constants.ConstantValueHeaders.DEVICE_OS)
                .header(AmericanExpressV62Constants.ConstantValueHeaders.HARDWARE_ID)
                .header(AmericanExpressV62Constants.ConstantValueHeaders.OS_VERSION)
                .header(
                        AmericanExpressV62Constants.Headers.INSTALLATION_ID,
                        persistentStorage.get(AmericanExpressV62Constants.Tags.INSTALLATION_ID))
                .header(AmericanExpressV62Constants.ConstantValueHeaders.TIMEZONE_OFFSET)
                .header(AmericanExpressV62Constants.Headers.LOCALE, config.getLocale());
    }

    protected RequestBuilder createRequestInSession(String uri) {
        return createRequest(uri)
                .header(
                        AmericanExpressV62Constants.Headers.SESSION,
                        sessionStorage.get(AmericanExpressV62Constants.Tags.SESSION_ID))
                .header(
                        AmericanExpressV62Constants.Headers.CUPCAKE,
                        sessionStorage.get(AmericanExpressV62Constants.Tags.CUPCAKE));
    }

    public LogonResponse logon(LogonRequest request) {

        return createRequest(AmericanExpressV62Constants.Urls.LOG_ON)
                .post(LogonResponse.class, request);
    }

    public TimelineResponse requestTimeline(TimelineRequest request) {
        return createRequestInSession(AmericanExpressV62Constants.Urls.TIMELINE)
                .post(TimelineResponse.class, request);
    }

    public TransactionResponse requestTransaction(TransactionsRequest request) {

        return createRequestInSession(AmericanExpressV62Constants.Urls.TRANSACTION)
                .post(TransactionResponse.class, request);
    }

    public ExtendResponse requestExtendSession() {
        return createRequestInSession(AmericanExpressV62Constants.Urls.EXTEND_SESSION)
                .post(ExtendResponse.class);
    }

    public LogoffResponse requestLogoff() {
        return createRequestInSession(AmericanExpressV62Constants.Urls.LOG_OUT)
                .post(LogoffResponse.class);
    }
}
