package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import se.tink.libraries.payment.enums.PaymentStatus;

@RunWith(JUnitParamsRunner.class)
public class SparebankPaymentStatusTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    private Object[] stringToSparebank() {
        return new Object[] {
            new Object[] {"ACcp", SparebankPaymentStatus.ACCP},
            new Object[] {"ACsC", SparebankPaymentStatus.ACSC},
            new Object[] {"acSp", SparebankPaymentStatus.ACSP},
            new Object[] {"Actc", SparebankPaymentStatus.ACTC},
            new Object[] {"aCWC", SparebankPaymentStatus.ACWC},
            new Object[] {"acWP", SparebankPaymentStatus.ACWP},
            new Object[] {"rcvD", SparebankPaymentStatus.RCVD},
            new Object[] {"PDNg", SparebankPaymentStatus.PDNG},
            new Object[] {"rjct", SparebankPaymentStatus.RJCT},
            new Object[] {"cANC", SparebankPaymentStatus.CANC}
        };
    }

    private Object[] sparebankToTink() {
        return new Object[] {
            new Object[] {SparebankPaymentStatus.ACCP, PaymentStatus.PENDING},
            new Object[] {SparebankPaymentStatus.ACSC, PaymentStatus.PAID},
            new Object[] {SparebankPaymentStatus.ACSP, PaymentStatus.PENDING},
            new Object[] {SparebankPaymentStatus.ACTC, PaymentStatus.PENDING},
            new Object[] {SparebankPaymentStatus.ACWC, PaymentStatus.PENDING},
            new Object[] {SparebankPaymentStatus.ACWP, PaymentStatus.PENDING},
            new Object[] {SparebankPaymentStatus.RCVD, PaymentStatus.PENDING},
            new Object[] {SparebankPaymentStatus.PDNG, PaymentStatus.PENDING},
            new Object[] {SparebankPaymentStatus.RJCT, PaymentStatus.REJECTED},
            new Object[] {SparebankPaymentStatus.CANC, PaymentStatus.CANCELLED}
        };
    }

    @Test
    @Parameters(method = "stringToSparebank")
    public void shouldReturnMatchingSparebankPaymentStatusBasedOnStringSearchTerm(
            String searchTerm, SparebankPaymentStatus expectedStatus) {
        assertEquals(expectedStatus, SparebankPaymentStatus.fromString(searchTerm));
    }

    @Test
    public void shouldThrowWhenNoMatchingSparebankPaymentStatusFound() {
        thrown.expect(IllegalStateException.class);

        SparebankPaymentStatus.fromString("something_that_is_not_a_payment_status");
    }

    @Test
    @Parameters(method = "sparebankToTink")
    public void shouldReturnMatchingTinkPaymentStatusBasedOnSparebankPaymentStatus(
            SparebankPaymentStatus searchStatus, PaymentStatus expectedStatus) {
        assertEquals(expectedStatus, SparebankPaymentStatus.mapToTinkPaymentStatus(searchStatus));
    }

    @Test
    public void shouldNotThrowForAnyPossibleValue() {
        for (SparebankPaymentStatus s : SparebankPaymentStatus.values()) {
            try {
                SparebankPaymentStatus.mapToTinkPaymentStatus(s);
            } catch (IllegalStateException e) {
                fail("mapToTinkPaymentStatus failed with status " + s);
            }
        }
    }
}
