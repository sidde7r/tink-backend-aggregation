package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.AspspPaymentStatus;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RunWith(JUnitParamsRunner.class)
public class FiduciaPaymentStatusMapperTest {

    FiduciaPaymentStatusMapper statusMapper = new FiduciaPaymentStatusMapper();

    @Test
    @Parameters(method = "pendingOrReveivedStates")
    public void shouldReturnSignedWhenRCVDorPNDGForRecurringPayments(
            AspspPaymentStatus bankPaymentStatus) {
        // given

        // when
        PaymentStatus paymentStatus =
                statusMapper.toTinkPaymentStatus(
                        bankPaymentStatus.getStatusText(), PaymentServiceType.PERIODIC);

        // then
        assertThat(paymentStatus).isEqualTo(PaymentStatus.SIGNED);
    }

    @Test
    @Parameters(method = "pendingOrReveivedStates")
    public void shouldReturnPendingWhenRCVDorPNDGForSinglePayments(
            AspspPaymentStatus bankPaymentStatus) {
        // given

        // when
        PaymentStatus paymentStatus =
                statusMapper.toTinkPaymentStatus(
                        bankPaymentStatus.getStatusText(), PaymentServiceType.SINGLE);

        // then
        assertThat(paymentStatus).isEqualTo(PaymentStatus.PENDING);
    }

    private Object[] pendingOrReveivedStates() {
        return new Object[] {AspspPaymentStatus.RECEIVED, AspspPaymentStatus.PENDING};
    }
}
