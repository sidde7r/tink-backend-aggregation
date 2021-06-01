package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.FiduciaPaymentMapper;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.rpc.CreatePaymentXmlRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.XmlConverter;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentProduct;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentService;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public class FiduciaPaymentApiClient extends FiduciaApiClient implements PaymentApiClient {

    private static final String PAYMENT_INITIATION = "/v1/{payment-service}/{payment-product}";
    private static final String FETCH_PAYMENT_STATUS =
            "/v1/{payment-service}/{payment-product}/{paymentId}/status";

    private final Credentials credentials;
    private final FiduciaPaymentMapper paymentMapper;

    public FiduciaPaymentApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String userIp,
            String serverUrl,
            RandomValueGenerator randomValueGenerator,
            Credentials credentials,
            FiduciaPaymentMapper paymentMapper) {
        super(client, persistentStorage, userIp, serverUrl, randomValueGenerator);
        this.credentials = credentials;
        this.paymentMapper = paymentMapper;
    }

    public CreatePaymentResponse createPayment(PaymentRequest paymentRequest) {
        CreatePaymentXmlRequest createPaymentRequest =
                PaymentServiceType.PERIODIC.equals(
                                paymentRequest.getPayment().getPaymentServiceType())
                        ? paymentMapper.getRecurringPaymentRequest(paymentRequest.getPayment())
                        : paymentMapper.getPaymentRequest(paymentRequest.getPayment());

        return createRequest(
                        createUrl(PAYMENT_INITIATION)
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_SERVICE,
                                        PaymentService.getPaymentService(
                                                paymentRequest
                                                        .getPayment()
                                                        .getPaymentServiceType()))
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_PRODUCT,
                                        "pain.001-"
                                                + PaymentProduct.getPaymentProduct(
                                                        paymentRequest
                                                                .getPayment()
                                                                .getPaymentScheme())))
                .header(
                        FiduciaConstants.HeaderKeys.PSU_ID,
                        credentials.getField(FiduciaConstants.CredentialKeys.PSU_ID))
                .type(MediaType.APPLICATION_XML_TYPE)
                .post(CreatePaymentResponse.class, XmlConverter.convertToXml(createPaymentRequest));
    }

    public FetchPaymentStatusResponse fetchPaymentStatus(PaymentRequest paymentRequest) {
        String paymentId = paymentRequest.getPayment().getUniqueId();
        return createRequest(
                        createUrl(FETCH_PAYMENT_STATUS)
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_SERVICE,
                                        PaymentService.getPaymentService(
                                                paymentRequest
                                                        .getPayment()
                                                        .getPaymentServiceType()))
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_PRODUCT,
                                        "pain.001-"
                                                + PaymentProduct.getPaymentProduct(
                                                        paymentRequest
                                                                .getPayment()
                                                                .getPaymentScheme()))
                                .parameter(PaymentConstants.PathVariables.PAYMENT_ID, paymentId))
                .get(FetchPaymentStatusResponse.class);
    }
}
