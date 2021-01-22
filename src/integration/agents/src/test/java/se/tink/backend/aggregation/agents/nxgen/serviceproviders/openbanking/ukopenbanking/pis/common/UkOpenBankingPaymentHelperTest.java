package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createClockMock;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createFutureDatePayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createTodayPayment;

import java.net.URI;
import java.time.Clock;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
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

    @Test
    public void testRevolutDomesticPaymentWithEmptyDebtor() {
        Payment payment =
                new Payment.Builder()
                        .withDebtor(new Debtor(null))
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                URI.create("sort-code://12345612345678"))))
                        .build();
        assertThat(UkOpenBankingPaymentHelper.getPaymentType(payment))
                .isEqualTo(PaymentType.DOMESTIC);
    }

    @Test
    public void testRevolutSepaPaymentWithEmptyDebtor() {
        Payment payment =
                new Payment.Builder()
                        .withDebtor(new Debtor(null))
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                URI.create("iban://SE4550000000058398257466"))))
                        .build();
        assertThat(UkOpenBankingPaymentHelper.getPaymentType(payment)).isEqualTo(PaymentType.SEPA);
    }
}
