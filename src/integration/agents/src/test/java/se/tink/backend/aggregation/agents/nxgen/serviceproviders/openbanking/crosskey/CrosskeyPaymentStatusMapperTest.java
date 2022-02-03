package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.enums.CrosskeyPaymentStatus;
import se.tink.libraries.payment.enums.PaymentStatus;

public class CrosskeyPaymentStatusMapperTest {

    @Test
    public void shouldMapToProperPaymentStatus() {

        assertEquals(
                CrosskeyPaymentStatus.mapToTinkPaymentStatus(
                        CrosskeyPaymentStatus.AWAITING_AUTHORISATION),
                PaymentStatus.PENDING);
        assertEquals(
                CrosskeyPaymentStatus.mapToTinkPaymentStatus(CrosskeyPaymentStatus.AUTHORISED),
                PaymentStatus.SIGNED);
        assertEquals(
                CrosskeyPaymentStatus.mapToTinkPaymentStatus(
                        CrosskeyPaymentStatus.ACCEPTED_SETTLEMENT_IN_PROCESS),
                PaymentStatus.PAID);
        assertEquals(
                CrosskeyPaymentStatus.mapToTinkPaymentStatus(CrosskeyPaymentStatus.UNKNOWN),
                PaymentStatus.UNDEFINED);
        assertEquals(
                CrosskeyPaymentStatus.mapToTinkPaymentStatus(CrosskeyPaymentStatus.CONSUMED),
                PaymentStatus.PAID);
        assertEquals(
                CrosskeyPaymentStatus.mapToTinkPaymentStatus(
                        CrosskeyPaymentStatus.ACCEPTED_SETTLEMENT_COMPLETED),
                PaymentStatus.PAID);
    }

    @Test
    public void shouldMapToProperCrosskeyStatus() {

        assertEquals(
                CrosskeyPaymentStatus.mapToCrosskeyPaymentStatus(PaymentStatus.PENDING),
                CrosskeyPaymentStatus.AWAITING_AUTHORISATION);
        assertEquals(
                CrosskeyPaymentStatus.mapToCrosskeyPaymentStatus(PaymentStatus.CREATED),
                CrosskeyPaymentStatus.AWAITING_AUTHORISATION);
        assertEquals(
                CrosskeyPaymentStatus.mapToCrosskeyPaymentStatus(PaymentStatus.SIGNED),
                CrosskeyPaymentStatus.AUTHORISED);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenStatusIsNotMapped() {
        // given
        String returnedStatus = "notKnownStatus";

        // then
        assertThatThrownBy(() -> CrosskeyPaymentStatus.fromString(returnedStatus))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        String.format(
                                "Cannot map Crosskey payment status : %s to Tink payment status.",
                                returnedStatus));
    }
}
