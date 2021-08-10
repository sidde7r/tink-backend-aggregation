package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.response.PaymentStatusXmlResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentProduct;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentService;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RequiredArgsConstructor
@Slf4j
public class FiduciaPaymentApiClient implements PaymentApiClient {

    private static final String PAYMENT_INITIATION = "/v1/{payment-service}/{payment-product}";
    private static final String FETCH_PAYMENT_STATUS =
            "/v1/{payment-service}/{payment-product}/{paymentId}/status";
    private static final String PAIN_001 = "pain.001-";

    private final FiduciaApiClient apiClient;
    private final Credentials credentials;
    private final FiduciaPaymentMapper paymentMapper;
    private final RandomValueGenerator randomValueGenerator;

    private static JAXBContext context;

    static {
        try {
            context = JAXBContext.newInstance(PaymentStatusXmlResponse.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Failed to initialize JAXBContext", e);
        }
    }

    public CreatePaymentResponse createPayment(PaymentRequest paymentRequest) {
        if (PaymentServiceType.PERIODIC == paymentRequest.getPayment().getPaymentServiceType()) {
            return createRecurringPayment(paymentRequest);
        } else {
            return createPaymentRequestBuilder(paymentRequest)
                    .type(MediaType.APPLICATION_XML_TYPE)
                    .post(
                            CreatePaymentResponse.class,
                            paymentMapper.getPaymentRequest(paymentRequest.getPayment()));
        }
    }

    public FetchPaymentStatusResponse fetchPaymentStatus(PaymentRequest paymentRequest) {
        String xmlResponse =
                apiClient
                        .createRequest(
                                createPaymentUrl(FETCH_PAYMENT_STATUS, paymentRequest)
                                        .parameter(
                                                PaymentConstants.PathVariables.PAYMENT_ID,
                                                paymentRequest.getPayment().getUniqueId()))
                        .get(String.class);
        PaymentStatusXmlResponse paymentStatusXmlResponse = tryParseXmlResponse(xmlResponse);

        return new FetchPaymentStatusResponse(
                paymentStatusXmlResponse.getCstmrPmtStsRpt().getOrgnlGrpInfAndSts().getGrpSts());
    }

    private URL createPaymentUrl(String path, PaymentRequest paymentRequest) {
        return apiClient
                .createUrl(path)
                .parameter(
                        PaymentConstants.PathVariables.PAYMENT_SERVICE,
                        PaymentService.getPaymentService(
                                paymentRequest.getPayment().getPaymentServiceType()))
                .parameter(
                        PaymentConstants.PathVariables.PAYMENT_PRODUCT,
                        PAIN_001
                                + PaymentProduct.getPaymentProduct(
                                        paymentRequest.getPayment().getPaymentScheme()));
    }

    private CreatePaymentResponse createRecurringPayment(PaymentRequest paymentRequest) {
        String boundary = randomValueGenerator.getUUID().toString();
        String recurringPaymentRequest =
                paymentMapper.getRecurringPaymentRequest(paymentRequest.getPayment(), boundary);
        return createPaymentRequestBuilder(paymentRequest)
                .header("content-type", "multipart/form-data; boundary=" + boundary)
                .post(CreatePaymentResponse.class, recurringPaymentRequest);
    }

    private RequestBuilder createPaymentRequestBuilder(PaymentRequest paymentRequest) {
        return apiClient
                .createRequest(createPaymentUrl(PAYMENT_INITIATION, paymentRequest))
                .header(
                        FiduciaConstants.HeaderKeys.PSU_ID,
                        credentials.getField(FiduciaConstants.CredentialKeys.PSU_ID));
    }

    private PaymentStatusXmlResponse tryParseXmlResponse(String xml) {
        try {
            Unmarshaller m = context.createUnmarshaller();
            return (PaymentStatusXmlResponse) m.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new IllegalStateException(
                    "The status response could not be parsed! Is it malformed?");
        }
    }
}
