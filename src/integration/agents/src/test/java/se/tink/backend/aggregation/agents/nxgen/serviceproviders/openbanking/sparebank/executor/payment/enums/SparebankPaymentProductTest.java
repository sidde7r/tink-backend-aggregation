package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.libraries.payment.enums.PaymentType;

@RunWith(JUnitParamsRunner.class)
public class SparebankPaymentProductTest {

    private static final String NOT_A_STATUS_STRING = "something_that_is_not_a_payment_status";

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
        Throwable throwable =
                catchThrowable(() -> SparebankPaymentProduct.fromString(NOT_A_STATUS_STRING));
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        String.format(
                                SparebankConstants.ErrorMessages.CANT_MAP_TO_PAYMENT_PRODUCT_ERROR,
                                NOT_A_STATUS_STRING));
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
        Throwable throwable =
                catchThrowable(
                        () ->
                                SparebankPaymentProduct.mapTinkPaymentTypeToSparebankPaymentProduct(
                                        searchStatus));
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        String.format(
                                SparebankConstants.ErrorMessages
                                        .MAPING_TO_TINK_PAYMENT_STATUS_ERROR,
                                searchStatus));
    }
}
