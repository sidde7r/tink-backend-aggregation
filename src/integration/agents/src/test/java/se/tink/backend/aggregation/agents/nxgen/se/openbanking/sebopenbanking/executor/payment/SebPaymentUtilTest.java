package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;

public class SebPaymentUtilTest {

    @Test(expected = ReferenceValidationException.class)
    public void testUnStructuredRemittanceInformationForLongMessage() throws PaymentException {
        SebPaymentUtil.validateUnStructuredRemittanceInformation("ThisIsALongMessage");
    }

    @Test
    public void testUnStructuredRemittanceInformationForNullMessage() throws PaymentException {
        SebPaymentUtil.validateUnStructuredRemittanceInformation(null);
    }

    @Test
    public void testUnStructuredRemittanceInformationForEmptyMessage() throws PaymentException {
        SebPaymentUtil.validateUnStructuredRemittanceInformation("");
    }

    @Test
    public void testUnStructuredRemittanceInformationForValidMessage() throws PaymentException {
        SebPaymentUtil.validateUnStructuredRemittanceInformation("message");
    }
}
