package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment;

import java.util.Base64;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils.BnpParibasSignatureHeaderProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.PispTokenRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class BnpParibasPaymentApiClient implements FrOpenBankingPaymentApiClient {
    private static final String TOKEN = "pis_token";

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final BnpParibasConfiguration configuration;
    private final BnpParibasSignatureHeaderProvider bnpParibasSignatureHeaderProvider;

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

        return client.request(new URL(configuration.getTokenUrl()))
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        BnpParibasBaseConstants.HeaderKeys.AUTHORIZATION,
                        BnpParibasBaseConstants.HeaderValues.BASIC
                                + Base64.getEncoder()
                                        .encodeToString(getAuthorizationString().getBytes()))
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class);
    }

    private String getAuthorizationString() {
        return configuration.getClientId() + ":" + configuration.getClientSecret();
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        return createRequest(createUrl(Urls.CREATE_PAYMENT))
                .post(CreatePaymentResponse.class, SerializationUtils.serializeToString(request));
    }

    @Override
    public String findPaymentId(String authorizationUrl) {
        return authorizationUrl.substring(authorizationUrl.lastIndexOf("=") + 1);
    }

    @Override
    public GetPaymentResponse getPayment(String paymentId) {
        return createRequest(createUrl(Urls.GET_PAYMENT).parameter(IdTags.PAYMENT_ID, paymentId))
                .get(GetPaymentResponse.class);
    }

    private URL createUrl(String path) {
        return new URL(configuration.getBaseUrl() + path);
    }

    private RequestBuilder createRequest(URL url) {
        String reqId = UUID.randomUUID().toString();
        String signature =
                bnpParibasSignatureHeaderProvider.buildSignatureHeader(
                        sessionStorage.get(StorageKeys.TOKEN), reqId, configuration);

        return client.request(url)
                .addBearerToken(getTokenFromStorage())
                .header(BnpParibasBaseConstants.HeaderKeys.SIGNATURE, signature)
                .header(BnpParibasBaseConstants.HeaderKeys.X_REQUEST_ID, reqId)
                .accept(MediaType.APPLICATION_JSON)
                .header(
                        BnpParibasBaseConstants.RegisterUtils.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON);
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.MISSING_TOKEN));
    }
}
