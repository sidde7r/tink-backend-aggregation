package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.Urls.GET_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.Urls.OAUTH_BASE_URL;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc.ConfirmPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.configuration.LaBanquePostaleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.PispTokenRequest;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class LaBanquePostalePaymentApiClient {
    private static final String TOKEN = "pis_token";

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final AgentConfiguration<LaBanquePostaleConfiguration> configuration;
    private final QsealcSigner qsealcSigner;

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
        PispTokenRequest request =
                new PispTokenRequest(
                        configuration.getProviderSpecificConfiguration().getClientId());

        return client.request(new URL(OAUTH_BASE_URL + GET_TOKEN))
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        HeaderKeys.AUTHORIZATION,
                        HeaderValues.BASIC
                                + Base64.getEncoder()
                                        .encodeToString(getAuthorizationString().getBytes()))
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class);
    }

    private String getAuthorizationString() {
        return configuration.getProviderSpecificConfiguration().getClientId()
                + ":"
                + configuration.getProviderSpecificConfiguration().getClientSecret();
    }

    public ConfirmPaymentResponse confirmPayment(String paymentId, String psuAuthorizationFactor) {
        return buildRequestWithSignature(
                        createUrl(Urls.CONFIRM_PAYMENT)
                                .parameter(IdTags.PAYMENT_ID, paymentId)
                                .toString(),
                        "")
                .post(
                        ConfirmPaymentResponse.class,
                        new ConfirmPaymentRequest(psuAuthorizationFactor));
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {

        RequestBuilder requestBuilder =
                buildRequestWithSignature(createUrl(Urls.PAYMENT_INITIATION).toString(), "");

        return requestBuilder.post(
                CreatePaymentResponse.class, SerializationUtils.serializeToString(request));
    }

    public String findPaymentId(String authorizationUrl) {
        final List<NameValuePair> queryParams;
        try {
            queryParams = new URIBuilder(authorizationUrl).getQueryParams();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot parse URL: " + authorizationUrl, e);
        }

        return queryParams.stream()
                .filter(param -> param.getName().equalsIgnoreCase("paymentRequestResourceId"))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot find paymentRequestRessourceId in URL: "
                                                + authorizationUrl));
    }

    public ConfirmPaymentResponse getPayment(String paymentId) {
        return buildRequestWithSignature(
                        createUrl(Urls.GET_PAYMENT)
                                .parameter(IdTags.PAYMENT_ID, paymentId)
                                .toString(),
                        "")
                .get(ConfirmPaymentResponse.class);
    }

    private URL createUrl(String path) {
        return new URL(LaBanquePostaleConstants.Urls.BASE_URL + path);
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.MISSING_TOKEN));
    }

    private RequestBuilder buildRequestWithSignature(final String url, final String payload) {
        final String digest = generateDigest(payload);
        final String requestId = UUID.randomUUID().toString();

        final OAuth2Token token = getTokenFromStorage();

        return client.request(url)
                .header(LaBanquePostaleConstants.HeaderKeys.PSU_DATE, LocalDate.now().toString())
                .header(
                        LaBanquePostaleConstants.HeaderKeys.SIGNATURE,
                        getAuthorization(digest, requestId))
                .header(HeaderKeys.CONTENT_TYPE, LaBanquePostaleConstants.HeaderValues.CONTENT_TYPE)
                .addBearerToken(token)
                .header(BerlinGroupConstants.HeaderKeys.X_REQUEST_ID, requestId);
    }

    private String generateDigest(final String data) {
        return Signature.DIGEST_PREFIX + Psd2Headers.calculateDigest(data);
    }

    private String getSignature(final String digest, String requestId) {
        final SignatureEntity signatureEntity = new SignatureEntity(digest, requestId);

        return qsealcSigner.getSignatureBase64(signatureEntity.toString().getBytes());
    }

    private String getAuthorization(final String digest, String requestId) {
        final String clientId = configuration.getProviderSpecificConfiguration().getClientId();

        return new AuthorizationEntity(clientId, getSignature(digest, requestId)).toString();
    }
}
