package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities.LogonRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.session.rpc.ExtendResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.session.rpc.LogoffResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AmericanExpressApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final AmericanExpressConfiguration config;

    public AmericanExpressApiClient(
            TinkHttpClient client, SessionStorage sessionStorage, AmericanExpressConfiguration config) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.config = config;
    }

    protected RequestBuilder createRequest(String uri) {

        URL url = new URL(AmericanExpressConstants.BASE_API + uri + config.getFace());
        return client
                .request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(AmericanExpressConstants.Headers.APP_ID, config.getAppId())
                .header(AmericanExpressConstants.Headers.USER_AGENT, config.getUserAgent())
                .header(
                        AmericanExpressConstants.Headers.CHARSET,
                        AmericanExpressConstants.HeaderValues.CHARSET)
                .header(
                        AmericanExpressConstants.Headers.CLIENT_TYPE,
                        AmericanExpressConstants.HeaderValues.CLIENT_TYPE)
                .header(
                        AmericanExpressConstants.Headers.CLIENT_VERSION,
                        config.getClientVersion())
                .header(
                        AmericanExpressConstants.Headers.DEVICE_ID,
                        sessionStorage.get(AmericanExpressConstants.Tags.HARDWARE_ID))
                .header(
                        AmericanExpressConstants.Headers.DEVICE_MODEL,
                        AmericanExpressConstants.HeaderValues.DEVICE_MODEL)
                .header(
                        AmericanExpressConstants.Headers.OS_BUILD,
                        AmericanExpressConstants.HeaderValues.OS_BUILD)
                .header(
                        AmericanExpressConstants.Headers.HARDWARE_ID,
                        sessionStorage.get(AmericanExpressConstants.Tags.HARDWARE_ID))
                .header(
                        AmericanExpressConstants.Headers.OS_VERSION,
                        AmericanExpressConstants.HeaderValues.OS_VERSION)
                .header(AmericanExpressConstants.Headers.FACE, config.getFace());
    }

    protected RequestBuilder createRequestInSession(String uri) {
        return createRequest(uri)
                .header(
                        AmericanExpressConstants.Headers.SESSION,
                        sessionStorage.get(AmericanExpressConstants.Tags.SESSION_ID))
                .header(
                        AmericanExpressConstants.Headers.CUPCAKE,
                        sessionStorage.get(AmericanExpressConstants.Tags.CUPCAKE));
    }

    public LogonResponse logon(LogonRequest request) {

        return createRequest(AmericanExpressConstants.Urls.LOG_IN)
                .post(LogonResponse.class, request);
    }

    public TimelineResponse requestTimeline(TimelineRequest request) {
        return createRequestInSession(AmericanExpressConstants.Urls.TIMELINE)
                .post(TimelineResponse.class, request);
    }

    public TransactionResponse requestTransaction(TransactionsRequest request) {
        // the response is in html format instead of Json
        String transactionString = createRequestInSession(AmericanExpressConstants.Urls.TRANSACTION)
                .post(String.class, request);
        TransactionResponse response = SerializationUtils.deserializeFromString(transactionString, TransactionResponse
                .class);
        return response;
    }

    public ExtendResponse requestExtendSession() {
        return createRequestInSession(AmericanExpressConstants.Urls.EXTEND_SESSION)
                .post(ExtendResponse.class);
    }

    public LogoffResponse requestLogoff() {
        return createRequestInSession(AmericanExpressConstants.Urls.LOG_OUT)
                .post(LogoffResponse.class);
    }
}
