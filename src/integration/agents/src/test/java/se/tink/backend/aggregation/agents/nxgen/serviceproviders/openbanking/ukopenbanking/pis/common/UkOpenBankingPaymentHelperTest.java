package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createClockMock;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createFutureDatePayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createTodayPayment;

import java.net.URI;
import java.time.Clock;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.pair.Pair;
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

    @SneakyThrows
    @Test
    public void shouldReturnDomesticPaymentTypeForTodayPayment() {
        // given
        final Payment paymentMock = createTodayPayment(this.clockMock);

        // when
        PaymentType paymentType = UkOpenBankingPaymentHelper.getPaymentType(paymentMock);

        // then
        assertThat(paymentType).isEqualTo(PaymentType.DOMESTIC);
    }

    @SneakyThrows
    @Test
    public void shouldReturnDomesticScheduledPaymentTypeForFuturePayment() {
        // given
        final Payment paymentMock = createFutureDatePayment(this.clockMock);

        // when
        PaymentType paymentType = UkOpenBankingPaymentHelper.getPaymentType(paymentMock);

        // then
        assertThat(paymentType).isEqualTo(PaymentType.DOMESTIC_FUTURE);
    }

    @SneakyThrows
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

    @SneakyThrows
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

    @Test
    public void testRevolutSepaPaymentWithNullValue() {
        // given
        final Payment paymentMock = createTodayPayment(this.clockMock);
        when(paymentMock.getCreditorAndDebtorAccountType())
                .thenReturn(new Pair<>(AccountIdentifierType.SORT_CODE, null));

        // when
        Throwable thrown =
                catchThrowable(() -> UkOpenBankingPaymentHelper.getPaymentType(paymentMock));

        // then
        Assertions.assertThat(thrown)
                .isExactlyInstanceOf(PaymentRejectedException.class)
                .hasFieldOrPropertyWithValue("InternalStatus", "INVALID_ACCOUNT_TYPE_COMBINATION");
    }
}
