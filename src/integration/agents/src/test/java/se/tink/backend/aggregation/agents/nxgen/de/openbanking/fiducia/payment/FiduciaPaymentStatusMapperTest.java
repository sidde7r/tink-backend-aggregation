package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RunWith(JUnitParamsRunner.class)
public class FiduciaPaymentStatusMapperTest {

    FiduciaPaymentStatusMapper statusMapper = new FiduciaPaymentStatusMapper();

    @Test
    @Parameters({"RCVD", "PDNG"})
    public void shouldReturnSignedWhenRCVDorPNDGForRecurringPayments(String bankPaymentStatus) {
        // given

        // when
        PaymentStatus paymentStatus =
                statusMapper.toTinkPaymentStatus(bankPaymentStatus, PaymentServiceType.PERIODIC);

        // then
        assertThat(paymentStatus).isEqualTo(PaymentStatus.SIGNED);
    }

    @Test
    @Parameters({"RCVD", "PDNG"})
    public void shouldReturnPendingWhenRCVDorPNDGForSinglePayments(String bankPaymentStatus) {
        // given

        // when
        PaymentStatus paymentStatus =
                statusMapper.toTinkPaymentStatus(bankPaymentStatus, PaymentServiceType.SINGLE);

        // then
        assertThat(paymentStatus).isEqualTo(PaymentStatus.PENDING);
    }
}
