package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.payment;

import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class CreditAgricolePaymentApiClient implements FrOpenBankingPaymentApiClient {

    private static final String SCOPE = "openid";
    private static final String GRANT_TYPE = "client_credentials";
    private static final String TOKEN = "pis_token";

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final CreditAgricoleBaseConfiguration configuration;

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
        OAuth2Token token = getToken().toTinkToken();
        sessionStorage.put(TOKEN, token);
    }

    private TokenResponse getToken() {
        final TokenRequest request =
                new TokenRequest.TokenRequestBuilder()
                        .scope(SCOPE)
                        .grantType(GRANT_TYPE)
                        .clientId(configuration.getClientId())
                        .build();

        return client.request(configuration.getBaseUrl() + ApiServices.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CORRELATION_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.CATS_CONSOMMATEUR, HeaderValues.CATS_CONSOMMATEUR)
                .header(HeaderKeys.CATS_CONSOMMATEURORIGINE, HeaderValues.CATS_CONSOMMATEURORIGINE)
                .header(HeaderKeys.CATS_CANAL, HeaderValues.CATS_CANAL)
                .post(TokenResponse.class, request.toData());
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        return createRequest(createUrl(ApiServices.CREATE_PAYMENT))
                .post(CreatePaymentResponse.class, SerializationUtils.serializeToString(request));
    }

    @Override
    public String findPaymentId(String authorizationUrl) {
        return authorizationUrl.substring(authorizationUrl.lastIndexOf("=") + 1);
    }

    @Override
    public GetPaymentResponse getPayment(String paymentId) {
        return createRequest(
                        createUrl(ApiServices.GET_PAYMENT).parameter(IdTags.PAYMENT_ID, paymentId))
                .get(GetPaymentResponse.class);
    }

    private URL createUrl(String path) {
        return new URL(configuration.getBaseUrl() + path);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromStorage());
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalArgumentException(ErrorMessages.UNABLE_LOAD_OAUTH_TOKEN));
    }
}
