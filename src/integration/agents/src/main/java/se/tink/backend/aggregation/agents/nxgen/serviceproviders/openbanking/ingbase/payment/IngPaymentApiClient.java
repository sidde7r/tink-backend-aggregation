package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngApiInputData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities.PaymentAuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities.PaymentSignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngPaymentStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentService;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public class IngPaymentApiClient extends IngBaseApiClient {

    private final StrongAuthenticationState strongAuthenticationState;
    private final String psuIpAddress;
    private final RandomValueGenerator randomValueGenerator;

    public IngPaymentApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            ProviderSessionCacheController providerSessionCacheController,
            MarketConfiguration marketConfiguration,
            QsealcSigner proxySigner,
            IngApiInputData ingApiInputData,
            AgentComponentProvider agentComponentProvider) {
        super(
                client,
                persistentStorage,
                providerSessionCacheController,
                marketConfiguration,
                proxySigner,
                ingApiInputData,
                agentComponentProvider);
        this.strongAuthenticationState = ingApiInputData.getStrongAuthenticationState();
        this.psuIpAddress = ingApiInputData.getUserAuthenticationData().getPsuIdAddress();
        this.randomValueGenerator = agentComponentProvider.getRandomValueGenerator();
    }

    public IngCreatePaymentResponse createPayment(
            IngCreatePaymentRequest request, PaymentServiceType paymentServiceType) {

        TokenResponse tokenResponse = getApplicationAccessToken();
        setApplicationTokenToSession(tokenResponse.toTinkToken());
        setClientIdToSession(tokenResponse.getClientId());

        RequestBuilder requestBuilder =
                buildRequestWithPaymentSignature(
                                createPaymentUrl(paymentServiceType),
                                IngBaseConstants.Signature.HTTP_METHOD_POST,
                                SerializationUtils.serializeToString(request))
                        .addBearerToken(getApplicationTokenFromSession())
                        .type(MediaType.APPLICATION_JSON)
                        .header(
                                IngBaseConstants.HeaderKeys.TPP_REDIRECT_URI,
                                getRedirectUrlWithState())
                        .header(IngBaseConstants.HeaderKeys.PSU_IP_ADDRESS, psuIpAddress);

        return requestBuilder.post(
                IngCreatePaymentResponse.class, SerializationUtils.serializeToString(request));
    }

    private URL createPaymentUrl(PaymentServiceType paymentServiceType) {
        return new URL(IngBaseConstants.Urls.CREATE_PAYMENT)
                .parameter(
                        IngBaseConstants.PathVariables.PAYMENT_SERVICE,
                        PaymentService.getPaymentService(paymentServiceType));
    }

    public IngPaymentStatusResponse getPaymentStatus(
            String paymentId, PaymentServiceType paymentServiceType) {
        RequestBuilder requestBuilder =
                buildRequestWithPaymentSignature(
                                getPaymentStatusUrl(paymentId, paymentServiceType),
                                IngBaseConstants.Signature.HTTP_METHOD_GET,
                                StringUtils.EMPTY)
                        .addBearerToken(getApplicationTokenFromSession())
                        .type(MediaType.APPLICATION_JSON)
                        .header(
                                IngBaseConstants.HeaderKeys.TPP_REDIRECT_URI,
                                getRedirectUrlWithState())
                        .header(IngBaseConstants.HeaderKeys.PSU_IP_ADDRESS, psuIpAddress);

        return requestBuilder.get(IngPaymentStatusResponse.class);
    }

    private URL getPaymentStatusUrl(String paymentId, PaymentServiceType paymentServiceType) {
        return new URL(IngBaseConstants.Urls.GET_PAYMENT_STATUS)
                .parameter(
                        IngBaseConstants.PathVariables.PAYMENT_SERVICE,
                        PaymentService.getPaymentService(paymentServiceType))
                .parameter(IngBaseConstants.PathVariables.PAYMENT_ID, paymentId);
    }

    public void cancelPayment(String paymentId, PaymentServiceType paymentServiceType) {
        RequestBuilder requestBuilder =
                buildRequestWithPaymentSignature(
                                getCancelPaymentUrl(paymentId, paymentServiceType),
                                IngBaseConstants.Signature.HTTP_METHOD_DELETE,
                                "")
                        .addBearerToken(getApplicationTokenFromSession())
                        .header(
                                IngBaseConstants.HeaderKeys.TPP_REDIRECT_URI,
                                getRedirectUrlWithState())
                        .header(IngBaseConstants.HeaderKeys.PSU_IP_ADDRESS, psuIpAddress);

        requestBuilder.delete();
    }

    private URL getCancelPaymentUrl(String paymentId, PaymentServiceType paymentServiceType) {
        return new URL(IngBaseConstants.Urls.DELETE_PAYMENT)
                .parameter(
                        IngBaseConstants.PathVariables.PAYMENT_SERVICE,
                        PaymentService.getPaymentService(paymentServiceType))
                .parameter(IngBaseConstants.PathVariables.PAYMENT_ID, paymentId);
    }

    private RequestBuilder buildRequestWithPaymentSignature(
            URL reqPath, String httpMethod, String payload) {
        String reqId = randomValueGenerator.getUUID().toString();
        String date = getFormattedDate();
        String digest = generateDigest(payload);

        PaymentSignatureEntity signatureEntity =
                new PaymentSignatureEntity(httpMethod, reqPath.toString(), date, digest, reqId);

        String signature =
                proxySigner
                        .sign(QsealcAlgorithm.RSA_SHA256, signatureEntity.toString().getBytes())
                        .getBase64Encoded();

        return buildRequest(reqId, date, digest, reqPath.toString())
                .header(IngBaseConstants.HeaderKeys.X_REQUEST_ID, reqId)
                .header(
                        IngBaseConstants.HeaderKeys.SIGNATURE,
                        new PaymentAuthorizationEntity(getClientIdFromSession(), signature)
                                .toString());
    }

    private URL getRedirectUrlWithState() {
        return new URL(redirectUrl)
                .queryParam(IngBaseConstants.QueryKeys.STATE, strongAuthenticationState.getState());
    }
}
