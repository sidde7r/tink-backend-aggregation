package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclAgent;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclHeaderValueProvider;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.ConfirmPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.PaymentRequestResource;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.api.Psd2Headers.Keys;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

@AllArgsConstructor
public class LclRequestFactory {

    private static final String APP_2_APP_REDIRECT_TOGGLE = "fr-lcl-pis-app2app-redirect";
    private static final String PAYMENT_ID_PATH_PLACEHOLDER = "paymentId";
    private static final String APP_2APP_CLIENT_TYPE = "retail";
    private static final String APP_2APP_CLIENT_TYPE_HEADER = "PSU-App2app-Client-Type";
    private static final String PISP_PATH = "/pisp";
    private static final String CONFIRM_PAYMENT_PATH =
            PISP_PATH + "/payment-requests/{paymentId}/confirmation";
    private static final String GET_PAYMENT_PATH = PISP_PATH + "/payment-requests/{paymentId}";
    private static final String CREATE_PAYMENT_PATH = PISP_PATH + "/payment-requests";
    private final LclHeaderValueProvider headerValueProvider;
    private final TinkHttpClient client;
    private final UnleashClient unleashClient;
    private final TokenFetcher tokenFetcher;
    private final PaymentBodyFactory paymentBodyFactory;

    public LclRequestFactory(
            LclHeaderValueProvider headerValueProvider,
            TinkHttpClient client,
            UnleashClient unleashClient,
            TokenFetcher tokenFetcher) {
        this(headerValueProvider, client, unleashClient, tokenFetcher, new PaymentBodyFactory());
    }

    public RequestBuilder getPaymentRequest(String paymentId) {
        RequestBuilder request =
                createRequestWithHeaders(
                        createPaymentPathWithPaymentId(GET_PAYMENT_PATH, paymentId));
        request = acceptJson(request);
        return request;
    }

    public RequestBuilder confirmPaymentRequest(String paymentId) {
        ConfirmPaymentRequest confirmPaymentRequest = paymentBodyFactory.createConfirmPaymentRequest();
        RequestBuilder request =
                createRequestWithHeaders(
                        createPaymentPathWithPaymentId(CONFIRM_PAYMENT_PATH, paymentId),
                        confirmPaymentRequest);
        request = acceptJson(request).body(confirmPaymentRequest);
        return request;
    }

    public RequestBuilder createPaymentRequest(CreatePaymentRequest paymentRequest) {
        PaymentRequestResource requestResource =
                paymentBodyFactory.createPaymentRequestResource(paymentRequest);

        RequestBuilder request =
                createRequestWithHeaders(createUrl(CREATE_PAYMENT_PATH), requestResource);

        if (unleashClient.isToggleEnabled(Toggle.of(APP_2_APP_REDIRECT_TOGGLE).build())) {
            request.header(APP_2APP_CLIENT_TYPE_HEADER, APP_2APP_CLIENT_TYPE);
        }

        request = acceptJson(request);
        request.body(paymentBodyFactory.serializeBody(requestResource));
        return request;
    }

    private RequestBuilder createRequestWithHeaders(URL url) {
        return createRequestWithHeaders(url, null);
    }

    private <T> RequestBuilder createRequestWithHeaders(URL url, T body) {
        final String requestId = UUID.randomUUID().toString();
        final String date = headerValueProvider.getDateHeaderValue();
        final String digest = headerValueProvider.getDigestHeaderValue(body);
        final String signature =
                headerValueProvider.getSignatureHeaderValue(requestId, date, digest);

        RequestBuilder request =
                client.request(url)
                        .header(Keys.X_REQUEST_ID, requestId)
                        .header(Keys.DATE, date)
                        .header(Keys.DIGEST, digest)
                        .header(Keys.SIGNATURE, signature);

        return request.addBearerToken(tokenFetcher.reuseTokenOrRefetch());
    }

    private URL createPaymentPathWithPaymentId(String paymentPath, String paymentId) {
        return createUrl(paymentPath).parameter(PAYMENT_ID_PATH_PLACEHOLDER, paymentId);
    }

    private RequestBuilder acceptJson(RequestBuilder requestBuilder) {
        return requestBuilder.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
    }

    private URL createUrl(String path) {
        return new URL(LclAgent.BASE_URL + path);
    }
}
