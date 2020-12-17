package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PaymentStatusResponseTest {

    private static final String DATE_ERROR_RESPONSE =
            "{\"paymentId\":\"STOHYD01201002140252163426000001\",\"transactionStatus\":\"RJCT\",\"tppMessages\":[\"The date when the money will reach the recipient is too close. Specify a later date.\"],\"_links\":{\"status\":{\"href\":\"/payments/swedish-domestic-private-bankgiros/STOHYD01201002140252163426000001/status\"}}}";

    private static final String UNSTRUCTURED_REMITTANCE_INFORMATION_REQUIRED =
            "{\"_links\":{\"status\":{\"href\":\"/payments/swedish-domestic-private-bankgiros/STOHYD01201201132244174447000001/status\"}},\"paymentId\":\"STOHYD01201201132244174447000001\",\"tppMessages\":[\"Bank Giro account does not accept OCR number, you have to enter message instead.\"],\"transactionStatus\":\"RJCT\"}";

    @Test(expected = DateValidationException.class)
    public void testDateTooCloseResponse() throws PaymentException {
        PaymentStatusResponse fetchPaymentResponse =
                SerializationUtils.deserializeFromString(
                        DATE_ERROR_RESPONSE, PaymentStatusResponse.class);
        fetchPaymentResponse.checkForErrors();
    }

    @Test(expected = ReferenceValidationException.class)
    public void testUnstructuredRemittanceInformationRequiredResponse() throws PaymentException {
        PaymentStatusResponse fetchPaymentResponse =
                SerializationUtils.deserializeFromString(
                        UNSTRUCTURED_REMITTANCE_INFORMATION_REQUIRED, PaymentStatusResponse.class);
        fetchPaymentResponse.checkForErrors();
    }
}
