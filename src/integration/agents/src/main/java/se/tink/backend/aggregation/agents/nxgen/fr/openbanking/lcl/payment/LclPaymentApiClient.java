package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclAgent;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclHeaderValueProvider;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclTokenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.ConfirmPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.GetPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc.PaymentRequestResource;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util.UrlParseUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class LclPaymentApiClient implements FrOpenBankingPaymentApiClient {

    private static final String PAYMENT_AUTHORIZATION_URL = "payment_authorization_url";
    private static final String PAYMENT_ID_PATH_PLACEHOLDER = "paymentId";
    private static final String PISP_PATH = "/pisp";
    private static final String CREATE_PAYMENT_PATH = PISP_PATH + "/payment-requests";
    private static final String GET_PAYMENT_PATH = PISP_PATH + "/payment-requests/{paymentId}";
    private static final String CONFIRM_PAYMENT_PATH =
            PISP_PATH + "/payment-requests/{paymentId}/confirmation";
    private static final String TOKEN = "pis_token";
    private static final String REDIRECT_URL_LOCAL_KEY = "FULL_REDIRECT_URL";
    private static final String PAYMENT_ID_LOCAL_KEY = "PAYMENT_ID";
    private static final String LCL_PAYMENT_STATUS_WAITING_FOR_CONFIRMATION = "ACTC";

    private final TinkHttpClient client;
    private final LclHeaderValueProvider headerValueProvider;
    private final SessionStorage sessionStorage;
    private final LclTokenApiClient tokenApiClient;

    @Override
    public void fetchToken() {
        if (!isTokenValid()) {
            getAndSaveToken();
        }
    }

    private boolean isTokenValid() {
        return sessionStorage
                .get(TOKEN, OAuth2Token.class)
                .map(OAuth2TokenBase::isValid)
                .orElse(false);
    }

    private void getAndSaveToken() {
        OAuth2Token token = getToken();
        sessionStorage.put(TOKEN, token);
    }

    private OAuth2Token getToken() {
        return tokenApiClient.getPispToken().toOauthToken();
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        PaymentRequestResource paymentRequestResource = new PaymentRequestResource(request);

        sessionStorage.put(
                REDIRECT_URL_LOCAL_KEY,
                request.getSupplementaryData().getSuccessfulReportUrl() + "&code=code");
        HttpResponse httpResponse =
                createRequestAndSetHeaders(createUrl(CREATE_PAYMENT_PATH), paymentRequestResource)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
                        .post(
                                HttpResponse.class,
                                SerializationUtils.serializeToString(paymentRequestResource));

        List<String> locationHeader = httpResponse.getHeaders().get("location");
        if (locationHeader.isEmpty()) {
            throw new IllegalArgumentException("location does not exist in the headers");
        }

        sessionStorage.put(PAYMENT_ID_LOCAL_KEY, UrlParseUtil.idFromUrl(locationHeader.get(0)));
        return httpResponse.getBody(CreatePaymentResponse.class);
    }

    @SneakyThrows
    @Override
    public String findPaymentId(String authorizationUrl) {
        URL url = new URL(authorizationUrl);

        sessionStorage.put(
                PAYMENT_AUTHORIZATION_URL,
                url.queryParam("client_id", LclAgent.CLIENT_ID)
                        .queryParam("redirect_uri", sessionStorage.get(REDIRECT_URL_LOCAL_KEY))
                        .toString());
        return sessionStorage.get(PAYMENT_ID_LOCAL_KEY);
    }

    @Override
    public GetPaymentResponse getPayment(String paymentId) {
        GetPaymentRequest getPaymentRequest =
                createRequestAndSetHeaders(
                                createUrl(GET_PAYMENT_PATH)
                                        .parameter(PAYMENT_ID_PATH_PLACEHOLDER, paymentId),
                                null)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
                        .get(GetPaymentRequest.class);
        String statusCode = getPaymentRequest.getPaymentRequest().getPaymentInformationStatus();
        return isStatusToBeConfirmed(statusCode)
                ? confirmPaymentRequest(paymentId).getPaymentRequest().toPaymentResponse()
                : getPaymentRequest.getPaymentRequest().toPaymentResponse();
    }

    private boolean isStatusToBeConfirmed(String statusCode) {
        return statusCode != null
                && statusCode.equalsIgnoreCase(LCL_PAYMENT_STATUS_WAITING_FOR_CONFIRMATION);
    }

    private GetPaymentRequest confirmPaymentRequest(String paymentId) {
        ConfirmPaymentRequest confirmPaymentRequest = new ConfirmPaymentRequest();
        return createRequestAndSetHeaders(
                        createUrl(CONFIRM_PAYMENT_PATH)
                                .parameter(PAYMENT_ID_PATH_PLACEHOLDER, paymentId),
                        confirmPaymentRequest)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(GetPaymentRequest.class, confirmPaymentRequest);
    }

    private URL createUrl(String path) {
        return new URL(LclAgent.BASE_URL + path);
    }

    private RequestBuilder createRequestAndSetHeaders(URL url, Object body) {
        final String requestId = UUID.randomUUID().toString();
        final String date = headerValueProvider.getDateHeaderValue();
        final String digest = headerValueProvider.getDigestHeaderValue(body);
        final String signature =
                headerValueProvider.getSignatureHeaderValue(requestId, date, digest);

        return client.request(url)
                .header(Psd2Headers.Keys.X_REQUEST_ID, requestId)
                .header(Psd2Headers.Keys.DATE, date)
                .header(Psd2Headers.Keys.DIGEST, digest)
                .header(Psd2Headers.Keys.SIGNATURE, signature)
                .addBearerToken(getTokenFromStorage());
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalArgumentException("Access token not found in storage."));
    }
}
