package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.domestic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createCreditorAccount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDebtor;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDebtorAccount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createExactCurrencyAmount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createInstructedAmount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponseForConsent;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createRemittanceInformation;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.common.ConverterTestBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.DebtorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.InstructedAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.common.RemittanceInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.dto.domestic.DomesticPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class DomesticPaymentConverterTest extends ConverterTestBase {

    private DomesticPaymentConverter domesticPaymentConverter;

    @Before
    public void setUp() {
        domesticPaymentConverter = new DomesticPaymentConverter();
    }

    @Test
    public void shouldGetDebtorAccount() {
        // given
        final Payment paymentMock = createPayment();

        // when
        final DebtorAccount returned = domesticPaymentConverter.getDebtorAccount(paymentMock);

        // then
        final DebtorAccount expected = createDebtorAccount();
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    public void shouldDebtorAccountBeNullWhenPaymentHasNoDebtor() {
        // given
        final Payment paymentMock = createPayment();

        when(paymentMock.getDebtor()).thenReturn(null);

        // when
        final DebtorAccount returned = domesticPaymentConverter.getDebtorAccount(paymentMock);

        // then
        assertThat(returned).isNull();
    }

    @Test
    public void shouldGetCreditorAccount() {
        // given
        final Payment paymentMock = createPayment();

        // when
        final CreditorAccount returned = domesticPaymentConverter.getCreditorAccount(paymentMock);

        // then
        final CreditorAccount expected = createCreditorAccount();
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    public void shouldCreditorAccountBeNullWhenPaymentHasNoCreditor() {
        // given
        final Payment paymentMock = createPayment();

        when(paymentMock.getCreditor()).thenReturn(null);

        // when
        final CreditorAccount returned = domesticPaymentConverter.getCreditorAccount(paymentMock);

        // then
        assertThat(returned).isNull();
    }

    @Test
    public void shouldGetRemittanceInformation() {
        // given
        final Payment paymentMock = createPayment();

        // when
        final RemittanceInformation returned =
                domesticPaymentConverter.getRemittanceInformation(paymentMock);

        // then
        final RemittanceInformation expected = createRemittanceInformation();
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    public void shouldRemittanceInformationBeEmptyStringWhenPaymentHasNone() {
        // given
        final Payment paymentMock = createPayment();

        when(paymentMock.getRemittanceInformation()).thenReturn(null);

        // when
        final RemittanceInformation returned =
                domesticPaymentConverter.getRemittanceInformation(paymentMock);

        // then
        assertThat(returned.getReference()).isEmpty();
        assertThat(returned.getUnstructured()).isEmpty();
    }

    @Test
    public void shouldGetInstructedAmount() {
        // given
        final Payment paymentMock = createPayment();

        // when
        final InstructedAmount returned = domesticPaymentConverter.getInstructedAmount(paymentMock);

        // then
        final InstructedAmount expected = createInstructedAmount();
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    public void shouldInstructedAmountBeNullWhenPaymentHasNone() {
        // given
        final Payment paymentMock = createPayment();

        when(paymentMock.getExactCurrencyAmountFromField()).thenReturn(null);

        // when
        final InstructedAmount returned = domesticPaymentConverter.getInstructedAmount(paymentMock);

        // then
        assertThat(returned).isNull();
    }

    @Test
    public void shouldConvertInstructedAmountToExactCurrencyAmount() {
        // given
        final InstructedAmount instructedAmountMock = createInstructedAmount();

        // when
        final ExactCurrencyAmount returned =
                domesticPaymentConverter.convertInstructedAmountToExactCurrencyAmount(
                        instructedAmountMock);

        // then
        final ExactCurrencyAmount expected = createExactCurrencyAmount();
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    public void shouldConvertDebtorAccountToDebtor() {
        // given
        final DebtorAccount debtorAccountMock = createDebtorAccount();

        // when
        final Debtor returned =
                domesticPaymentConverter.convertDebtorAccountToDebtor(debtorAccountMock);

        // then
        final Debtor expected = createDebtor();

        validateAccountIdentifiersAreEqual(
                returned.getAccountIdentifier(), expected.getAccountIdentifier());
        assertThat(returned.getAccountIdentifierType())
                .isEqualTo(expected.getAccountIdentifierType());
        assertThat(returned.getAccountNumber()).isEqualTo(expected.getAccountNumber());
    }

    @Test
    public void shouldConvertConsentResponseDtoToTinkPaymentResponse() {
        // given
        final DomesticPaymentConsentResponse responseMock = createDomesticPaymentConsentResponse();

        // when
        final PaymentResponse returned =
                domesticPaymentConverter.convertConsentResponseDtoToTinkPaymentResponse(
                        responseMock);

        // then
        final PaymentResponse expected = createPaymentResponseForConsent();

        validatePaymentResponsesAreEqual(returned, expected);
    }

    @Test
    public void shouldConvertResponseDtoToPaymentResponse() {
        // given
        final DomesticPaymentResponse responseMock = createDomesticPaymentResponse();

        // when
        final PaymentResponse returned =
                domesticPaymentConverter.convertResponseDtoToPaymentResponse(responseMock);

        // then
        final PaymentResponse expected = createPaymentResponse();

        validatePaymentResponsesAreEqual(returned, expected);
    }

    private static void validatePaymentResponsesAreEqual(
            PaymentResponse returned, PaymentResponse expected) {
        validatePaymentsAreEqual(returned.getPayment(), expected.getPayment());
        assertThat(returned.getStorage()).isEqualTo(expected.getStorage());
    }
}
