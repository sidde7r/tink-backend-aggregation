package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createCreditorAccount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDebtor;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDebtorAccount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createDomesticPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createExactCurrencyAmount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createInstructedAmount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createNoTypeRemittanceInformation;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createNoTypeRemittanceInformationDto;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentResponseForConsent;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentWithRemittanceInfo;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createReferenceRemittanceInformation;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createReferenceRemittanceInformationDto;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createUnstructuredRemittanceInformation;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createUnstructuredRemittanceInformationDto;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingTestValidator.validateAccountIdentifiersAreEqual;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingTestValidator.validatePaymentResponsesForDomesticPaymentAreEqual;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.CreditorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.DebtorAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.InstructedAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.RemittanceInformationDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.dto.DomesticPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DomesticPaymentConverterTest {

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
    public void shouldDebtorAccountBeNullWhenPaymentDebtorHasNoAccountifier() {
        // given
        final Payment paymentMock = createPayment();

        when(paymentMock.getDebtor()).thenReturn(new Debtor(null));

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
    public void shouldGetUnstructuredRemittanceInformation() {
        // given
        final RemittanceInformation remittanceInformation =
                createUnstructuredRemittanceInformation();
        final Payment paymentMock = createPaymentWithRemittanceInfo(remittanceInformation);

        // when
        final RemittanceInformationDto returned =
                domesticPaymentConverter.getRemittanceInformationDto(paymentMock);

        // then
        final RemittanceInformationDto expected = createUnstructuredRemittanceInformationDto();
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    public void shouldGetReferenceRemittanceInformation() {
        // given
        final RemittanceInformation remittanceInformation = createReferenceRemittanceInformation();
        final Payment paymentMock = createPaymentWithRemittanceInfo(remittanceInformation);

        // when
        final RemittanceInformationDto returned =
                domesticPaymentConverter.getRemittanceInformationDto(paymentMock);

        // then
        final RemittanceInformationDto expected = createReferenceRemittanceInformationDto();
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    public void shouldGetNoTypeRemittanceInformation() {
        // given
        final RemittanceInformation remittanceInformation = createNoTypeRemittanceInformation();
        final Payment paymentMock = createPaymentWithRemittanceInfo(remittanceInformation);

        // when
        final RemittanceInformationDto returned =
                domesticPaymentConverter.getRemittanceInformationDto(paymentMock);

        // then
        final RemittanceInformationDto expected = createNoTypeRemittanceInformationDto();
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    public void shouldRemittanceInformationBeEmptyStringWhenPaymentHasNone() {
        // given
        final Payment paymentMock = createPayment();

        when(paymentMock.getRemittanceInformation()).thenReturn(null);

        // when
        final RemittanceInformationDto returned =
                domesticPaymentConverter.getRemittanceInformationDto(paymentMock);

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

        validatePaymentResponsesForDomesticPaymentAreEqual(returned, expected);
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

        validatePaymentResponsesForDomesticPaymentAreEqual(returned, expected);
    }
}
