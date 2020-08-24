package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclHeaderValueProvider;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.PispTokenRequest;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class LclPaymentApiClient implements FrOpenBankingPaymentApiClient {

    private static final String TOKEN_PATH = "/token";
    private static final String PISP_PATH = "/pisp";
    private static final String CREATE_PAYMENT_PATH = PISP_PATH + "/payment-requests";
    private static final String GET_PAYMENT_PATH = PISP_PATH + "/payment-requests/{paymentId}";
    private static final String TOKEN = "pis_token";

    private final TinkHttpClient client;
    private final LclHeaderValueProvider headerValueProvider;
    private final SessionStorage sessionStorage;
    private final AgentConfiguration<LclConfiguration> configuration;

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

    private TokenResponseDto getToken() {
        PispTokenRequest request =
                new PispTokenRequest(
                        configuration.getProviderSpecificConfiguration().getClientId());

        return client.request(createUrl(TOKEN_PATH))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponseDto.class, request);
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        return createRequestAndSetHeaders(createUrl(CREATE_PAYMENT_PATH), request)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(CreatePaymentResponse.class, SerializationUtils.serializeToString(request));
    }

    @Override
    public String findPaymentId(String authorizationUrl) {
        // FIXME
        return authorizationUrl.substring(authorizationUrl.lastIndexOf("/") + 1);
    }

    @Override
    public GetPaymentResponse getPayment(String paymentId) {
        return client.request(createUrl(GET_PAYMENT_PATH).parameter("paymentId", paymentId))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(GetPaymentResponse.class);
    }

    private URL createUrl(String path) {
        return new URL(configuration.getProviderSpecificConfiguration().getBaseUrl() + path);
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
