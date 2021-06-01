package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.payment;

import com.google.common.base.Strings;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.PispTokenRequest;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
@Slf4j
public class BpceGroupPaymentApiClient implements FrOpenBankingPaymentApiClient {

    private static final String TOKEN = "pis_token";
    private static final String TOKEN_PATH = "/stet/psd2/oauth/token";

    private static final String BASE_PATH = "/stet/psd2/v1";
    private static final String CREATE_PAYMENT = "/payment-requests";
    private static final String GET_PAYMENT = "/payment-requests/{paymentId}";

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final BpceGroupConfiguration configuration;
    private final BpceGroupSignatureHeaderGenerator bpceGroupSignatureHeaderGenerator;

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
        OAuth2Token token = getToken().toOauthToken();
        sessionStorage.put(TOKEN, token);
    }

    private TokenResponse getToken() {
        PispTokenRequest request = new PispTokenRequest(configuration.getClientId());

        final HttpResponse response =
                client.request(createUrl(TOKEN_PATH))
                        .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(HttpResponse.class);

        return response.getBody(TokenResponse.class);
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        final RequestBuilder requestBuilder =
                client.request(createUrl(BASE_PATH, CREATE_PAYMENT))
                        .body(request, MediaType.APPLICATION_JSON);

        return sendRequestPrintingCorrelationId(
                requestBuilder, HttpMethod.POST, CreatePaymentResponse.class);
    }

    @Override
    public String findPaymentId(String authorizationUrl) {
        final List<NameValuePair> queryParams;
        try {
            queryParams = new URIBuilder(authorizationUrl).getQueryParams();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot parse URL: " + authorizationUrl, e);
        }

        return queryParams.stream()
                .filter(param -> param.getName().equalsIgnoreCase("paymentRequestRessourceId"))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot find paymentRequestRessourceId in URL: "
                                                + authorizationUrl));
    }

    @Override
    public GetPaymentResponse getPayment(String paymentId) {
        final RequestBuilder requestBuilder =
                client.request(createUrl(BASE_PATH, GET_PAYMENT).parameter("paymentId", paymentId));

        return sendRequestPrintingCorrelationId(
                requestBuilder, HttpMethod.GET, GetPaymentResponse.class);
    }

    private URL createUrl(String path) {
        return new URL(configuration.getServerUrl() + path);
    }

    private URL createUrl(String base, String path) {
        return new URL(configuration.getServerUrl() + base + path);
    }

    private <T> T sendRequestPrintingCorrelationId(
            RequestBuilder requestBuilder, HttpMethod httpMethod, Class<T> clazz) {
        HttpResponse clientResponse =
                addHeadersToRequest(requestBuilder, httpMethod)
                        .method(httpMethod, HttpResponse.class);
        String xCorrelationId = clientResponse.getHeaders().getFirst("X-CorrelationID");
        if (!Strings.isNullOrEmpty(xCorrelationId)) {
            log.info(String.format("X-CorrelationID: %s", xCorrelationId));
        }
        return clientResponse.getBody(clazz);
    }

    private RequestBuilder addHeadersToRequest(
            RequestBuilder requestBuilder, HttpMethod httpMethod) {
        final String requestId = UUID.randomUUID().toString();
        final String signature =
                bpceGroupSignatureHeaderGenerator.buildSignatureHeader(
                        httpMethod, requestBuilder.getUrl(), requestId);

        return requestBuilder
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(Psd2Headers.Keys.SIGNATURE, signature)
                .header(Psd2Headers.Keys.X_REQUEST_ID, requestId)
                .addBearerToken(getTokenFromStorage());
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalArgumentException("missing token"));
    }
}
