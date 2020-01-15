package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import se.tink.libraries.payment.enums.PaymentType;

@RunWith(JUnitParamsRunner.class)
public class SparebankPaymentProductTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    private Object[] stringToSparebank() {
        return new Object[] {
            new Object[] {
                "nOrwegiaN-DOMEStiC-cREdIT-TRansFeRS",
                SparebankPaymentProduct.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER
            },
            new Object[] {
                "CRoSS-bOrDER-CREdiT-tRAnsfERs",
                SparebankPaymentProduct.CROSS_BORDER_CREDIT_TRANSFER
            },
            new Object[] {"sepa-CREDIt-TRanSfeRS", SparebankPaymentProduct.SEPA_CREDIT_TRANSFER}
        };
    }

    private Object[] tinkToSparebank() {
        return new Object[] {
            new Object[] {
                PaymentType.DOMESTIC, SparebankPaymentProduct.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER
            },
            new Object[] {
                PaymentType.INTERNATIONAL, SparebankPaymentProduct.CROSS_BORDER_CREDIT_TRANSFER,
            },
            new Object[] {PaymentType.SEPA, SparebankPaymentProduct.SEPA_CREDIT_TRANSFER}
        };
    }

    private Object[] unsupportedTinkPaymentProducts() {
        List<PaymentType> supportedTinkPaymentTypes =
                Arrays.asList(PaymentType.DOMESTIC, PaymentType.INTERNATIONAL, PaymentType.SEPA);
        return Arrays.stream(PaymentType.values())
                .filter(x -> !supportedTinkPaymentTypes.contains(x))
                .toArray();
    }

    @Test
    @Parameters(method = "stringToSparebank")
    public void shouldReturnMatchingSparebankPaymentProductBasedOnStringSearchTerm(
            String searchTerm, SparebankPaymentProduct expectedStatus) {
        assertEquals(expectedStatus, SparebankPaymentProduct.fromString(searchTerm));
    }

    @Test
    public void shouldThrowWhenNoMatchingSparebankPaymentProductFound() {
        thrown.expect(IllegalStateException.class);

        SparebankPaymentProduct.fromString("something_that_is_not_a_payment_status");
    }

    @Test
    @Parameters(method = "tinkToSparebank")
    public void shouldReturnMatchingSparebankPaymentProductBasedOnTinkPaymentType(
            PaymentType searchStatus, SparebankPaymentProduct expectedStatus) {
        assertEquals(
                expectedStatus,
                SparebankPaymentProduct.mapTinkPaymentTypeToSparebankPaymentProduct(searchStatus));
    }

    @Test
    @Parameters(method = "unsupportedTinkPaymentProducts")
    public void shouldThrowWhenMapToTinkPaymentProductCalledWithUnsupportedTinkPaymentProduct(
            PaymentType searchStatus) {
        thrown.expect(IllegalStateException.class);
        SparebankPaymentProduct.mapTinkPaymentTypeToSparebankPaymentProduct(searchStatus);
    }
}
