package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities.PaymentAuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities.PaymentSignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IngPaymentApiClient extends IngBaseApiClient {

    public IngPaymentApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String market,
            ProviderSessionCacheController providerSessionCacheController,
            boolean isManualAuthentication,
            MarketConfiguration marketConfiguration,
            QsealcSigner proxySigner) {
        super(
                client,
                persistentStorage,
                market,
                providerSessionCacheController,
                isManualAuthentication,
                marketConfiguration,
                proxySigner);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {

        TokenResponse tokenResponse = getApplicationAccessToken();
        setApplicationTokenToSession(tokenResponse.toTinkToken());
        setClientIdToSession(tokenResponse.getClientId());

        RequestBuilder requestBuilder =
                buildRequestWithPaymentSignature(
                                IngBaseConstants.Urls.PAYMENT_INITIATION,
                                IngBaseConstants.Signature.HTTP_METHOD_POST,
                                SerializationUtils.serializeToString(request))
                        .addBearerToken(getApplicationTokenFromSession())
                        .type(MediaType.APPLICATION_JSON)
                        .header(IngBaseConstants.HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                        .header(IngBaseConstants.HeaderKeys.PSU_ID_ADDRESS, psuIdAddress);

        return requestBuilder.post(
                CreatePaymentResponse.class, SerializationUtils.serializeToString(request));
    }

    public GetPaymentResponse getPayment(String paymentId) {

        RequestBuilder requestBuilder =
                buildRequestWithPaymentSignature(
                                new URL(IngBaseConstants.Urls.GET_PAYMENT_STATUS)
                                        .parameter(IngBaseConstants.IdTags.PAYMENT_ID, paymentId)
                                        .toString(),
                                IngBaseConstants.Signature.HTTP_METHOD_GET,
                                StringUtils.EMPTY)
                        .addBearerToken(getApplicationTokenFromSession())
                        .type(MediaType.APPLICATION_JSON)
                        .header(IngBaseConstants.HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                        .header(IngBaseConstants.HeaderKeys.PSU_ID_ADDRESS, psuIdAddress);

        return requestBuilder.get(GetPaymentResponse.class);
    }

    private RequestBuilder buildRequestWithPaymentSignature(
            String reqPath, String httpMethod, String payload) {
        String reqId = Psd2Headers.getRequestId();
        String date = getFormattedDate();
        String digest = generateDigest(payload);

        PaymentSignatureEntity signatureEntity =
                new PaymentSignatureEntity(httpMethod, reqPath, date, digest, reqId);

        return buildRequest(reqId, date, digest, reqPath)
                .header(IngBaseConstants.HeaderKeys.X_REQUEST_ID, reqId)
                .header(
                        IngBaseConstants.HeaderKeys.SIGNATURE,
                        new PaymentAuthorizationEntity(
                                        getClientIdFromSession(),
                                        proxySigner.getSignatureBase64(
                                                signatureEntity.toString().getBytes()))
                                .toString());
    }
}
