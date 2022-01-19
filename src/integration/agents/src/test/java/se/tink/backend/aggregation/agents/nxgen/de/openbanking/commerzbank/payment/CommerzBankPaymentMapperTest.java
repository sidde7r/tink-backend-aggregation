package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class CommerzBankPaymentMapperTest {

    private static final String TEST_IBAN_SOURCE = "NL07INGB8430666915";
    private static final String TEST_IBAN_DESTINATION = "NL60ABNA7467539436";

    private final CommerzBankPaymentMapper paymentMapper = new CommerzBankPaymentMapper();

    @Test
    public void shouldMapSinglePaymentRequestCorrectly() {
        // given
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("remittanceInfo");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Payment payment =
                new Builder()
                        .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                        .withPaymentServiceType(PaymentServiceType.SINGLE)
                        .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                        .withCreditor(
                                new Creditor(
                                        new IbanIdentifier(TEST_IBAN_DESTINATION), "creditor name"))
                        .withDebtor(new Debtor(new IbanIdentifier(TEST_IBAN_SOURCE)))
                        .withRemittanceInformation(remittanceInformation)
                        .build();

        // when
        CreatePaymentRequest paymentRequest = paymentMapper.getPaymentRequest(payment);

        // then
        assertThat(paymentRequest.getInstructedAmount().getAmount()).isEqualTo("1.0");
        assertThat(paymentRequest.getInstructedAmount().getCurrency()).isEqualTo("EUR");
        assertThat(paymentRequest.getCreditorName()).isEqualTo("creditor name");
        assertThat(paymentRequest.getCreditorAccount().getIban()).isEqualTo(TEST_IBAN_DESTINATION);
        assertThat(paymentRequest.getDebtorAccount().getIban()).isEqualTo(TEST_IBAN_SOURCE);
        assertThat(paymentRequest.getRemittanceInformationUnstructured())
                .isEqualTo("remittanceInfo");
    }

    @Test
    public void shouldFailToBuildPaymentRequestWhenNoProperIdentifier() {
        // given
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("remittanceInfo");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Payment payment =
                new Builder()
                        .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                        .withPaymentServiceType(PaymentServiceType.SINGLE)
                        .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                        .withDebtor(new Debtor(new BbanIdentifier("asdf")))
                        .withCreditor(new Creditor(new BbanIdentifier("asdf"), "creditor name"))
                        .withRemittanceInformation(remittanceInformation)
                        .build();
        // when
        Throwable throwable = catchThrowable(() -> paymentMapper.getPaymentRequest(payment));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Could not find account identifier of expected type! Expected: IbanIdentifier");
    }

    @Test
    public void shouldReturnNullWhenAskedForRecurringPaymentRequest() {
        // given
        Payment payment = new Payment.Builder().build();
        // when
        CreatePaymentRequest recurringPaymentRequest =
                paymentMapper.getRecurringPaymentRequest(payment);
        // then
        assertThat(recurringPaymentRequest).isNull();
    }
}
