package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createClockMock;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createFutureDatePayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createTodayPayment;

import java.time.Clock;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class UkOpenBankingPaymentHelperTest {

    private Clock clockMock;

    @Before
    public void setUp() {
        clockMock = createClockMock();
    }

    @Test
    public void shouldReturnDomesticPaymentTypeForTodayPayment() {
        // given
        final Payment paymentMock = createTodayPayment(this.clockMock);

        // when
        PaymentType paymentType = UkOpenBankingPaymentHelper.getPaymentType(paymentMock);

        // then
        assertThat(paymentType).isEqualTo(PaymentType.DOMESTIC);
    }

    @Test
    public void shouldReturnDomesticScheduledPaymentTypeForFuturePayment() {
        // given
        final Payment paymentMock = createFutureDatePayment(this.clockMock);

        // when
        PaymentType paymentType = UkOpenBankingPaymentHelper.getPaymentType(paymentMock);

        // then
        assertThat(paymentType).isEqualTo(PaymentType.DOMESTIC_FUTURE);
    }
}
