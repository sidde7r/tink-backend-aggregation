package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities.PaymentAuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities.PaymentSignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngPaymentStatusResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IngPaymentApiClient extends IngBaseApiClient {

    private final StrongAuthenticationState strongAuthenticationState;

    public IngPaymentApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String market,
            ProviderSessionCacheController providerSessionCacheController,
            boolean isManualAuthentication,
            MarketConfiguration marketConfiguration,
            QsealcSigner proxySigner,
            StrongAuthenticationState strongAuthenticationState) {
        super(
                client,
                persistentStorage,
                market,
                providerSessionCacheController,
                isManualAuthentication,
                marketConfiguration,
                proxySigner);
        this.strongAuthenticationState = strongAuthenticationState;
    }

    public IngCreatePaymentResponse createPayment(IngCreatePaymentRequest request) {

        TokenResponse tokenResponse = getApplicationAccessToken();
        setApplicationTokenToSession(tokenResponse.toTinkToken());
        setClientIdToSession(tokenResponse.getClientId());

        RequestBuilder requestBuilder =
                buildRequestWithPaymentSignature(
                                new URL(IngBaseConstants.Urls.CREATE_PAYMENT),
                                IngBaseConstants.Signature.HTTP_METHOD_POST,
                                SerializationUtils.serializeToString(request))
                        .addBearerToken(getApplicationTokenFromSession())
                        .type(MediaType.APPLICATION_JSON)
                        .header(
                                IngBaseConstants.HeaderKeys.TPP_REDIRECT_URI,
                                getRedirectUrlWithState())
                        .header(IngBaseConstants.HeaderKeys.PSU_ID_ADDRESS, psuIdAddress);

        return requestBuilder.post(
                IngCreatePaymentResponse.class, SerializationUtils.serializeToString(request));
    }

    public IngPaymentStatusResponse getPaymentStatus(String paymentId) {
        RequestBuilder requestBuilder =
                buildRequestWithPaymentSignature(
                                new URL(IngBaseConstants.Urls.GET_PAYMENT_STATUS)
                                        .parameter(IngBaseConstants.IdTags.PAYMENT_ID, paymentId),
                                IngBaseConstants.Signature.HTTP_METHOD_GET,
                                StringUtils.EMPTY)
                        .addBearerToken(getApplicationTokenFromSession())
                        .type(MediaType.APPLICATION_JSON)
                        .header(
                                IngBaseConstants.HeaderKeys.TPP_REDIRECT_URI,
                                getRedirectUrlWithState())
                        .header(IngBaseConstants.HeaderKeys.PSU_ID_ADDRESS, psuIdAddress);

        return requestBuilder.get(IngPaymentStatusResponse.class);
    }

    public void cancelPayment(String paymentId) {
        RequestBuilder requestBuilder =
                buildRequestWithPaymentSignature(
                                new URL(IngBaseConstants.Urls.DELETE_PAYMENT)
                                        .parameter(IngBaseConstants.IdTags.PAYMENT_ID, paymentId),
                                IngBaseConstants.Signature.HTTP_METHOD_DELETE,
                                "")
                        .addBearerToken(getApplicationTokenFromSession())
                        .header(
                                IngBaseConstants.HeaderKeys.TPP_REDIRECT_URI,
                                getRedirectUrlWithState())
                        .header(IngBaseConstants.HeaderKeys.PSU_ID_ADDRESS, psuIdAddress);

        requestBuilder.delete();
    }

    private RequestBuilder buildRequestWithPaymentSignature(
            URL reqPath, String httpMethod, String payload) {
        String reqId = Psd2Headers.getRequestId();
        String date = getFormattedDate();
        String digest = generateDigest(payload);

        PaymentSignatureEntity signatureEntity =
                new PaymentSignatureEntity(httpMethod, reqPath.toString(), date, digest, reqId);

        return buildRequest(reqId, date, digest, reqPath.toString())
                .header(IngBaseConstants.HeaderKeys.X_REQUEST_ID, reqId)
                .header(
                        IngBaseConstants.HeaderKeys.SIGNATURE,
                        new PaymentAuthorizationEntity(
                                        getClientIdFromSession(),
                                        proxySigner.getSignatureBase64(
                                                signatureEntity.toString().getBytes()))
                                .toString());
    }

    private URL getRedirectUrlWithState() {
        return new URL(redirectUrl)
                .queryParam(IngBaseConstants.QueryKeys.STATE, strongAuthenticationState.getState());
    }
}
