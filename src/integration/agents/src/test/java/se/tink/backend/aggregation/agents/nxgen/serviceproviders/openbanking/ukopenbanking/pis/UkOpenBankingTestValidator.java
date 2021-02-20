package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Ignore;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.payment.rpc.Payment;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UkOpenBankingTestValidator {

    public static void validatePaymentResponsesForDomesticPaymentAreEqual(
            PaymentResponse returned, PaymentResponse expected) {
        validatePaymentsAreEqual(returned.getPayment(), expected.getPayment());
        assertThat(returned.getStorage()).isEqualTo(expected.getStorage());
    }

    public static void validatePaymentResponsesForDomesticScheduledPaymentsAreEqual(
            PaymentResponse returned, PaymentResponse expected) {
        validatePaymentsAreEqual(returned.getPayment(), expected.getPayment());
        AssertionsForClassTypes.assertThat(returned.getPayment().getExecutionDate())
                .isEqualTo(expected.getPayment().getExecutionDate());
        AssertionsForClassTypes.assertThat(returned.getStorage()).isEqualTo(expected.getStorage());
    }

    public static void validateAccountIdentifiersAreEqual(
            AccountIdentifier returned, AccountIdentifier expected) {
        assertThat(returned.getIdentifier()).isEqualTo(expected.getIdentifier());
        assertThat(returned.getType()).isEqualTo(expected.getType());
    }

    private static void validatePaymentsAreEqual(Payment returned, Payment expected) {
        validateAccountIdentifiersAreEqual(
                returned.getCreditor().getAccountIdentifier(),
                expected.getCreditor().getAccountIdentifier());
        assertThat(returned.getCreditor().getAccountIdentifierType())
                .isEqualTo(expected.getCreditor().getAccountIdentifierType());
        assertThat(returned.getCreditor().getAccountNumber())
                .isEqualTo(expected.getCreditor().getAccountNumber());
        assertThat(returned.getCreditor().getName()).isEqualTo(expected.getCreditor().getName());

        validateAccountIdentifiersAreEqual(
                returned.getDebtor().getAccountIdentifier(),
                expected.getDebtor().getAccountIdentifier());
        assertThat(returned.getDebtor().getAccountNumber())
                .isEqualTo(expected.getDebtor().getAccountNumber());

        assertThat(returned.getCurrency()).isEqualTo(expected.getCurrency());
        assertThat(returned.getUniqueId()).isEqualTo(expected.getUniqueId());
        assertThat(returned.getUniqueIdForUKOPenBanking())
                .isEqualTo(expected.getUniqueIdForUKOPenBanking());
        assertThat(returned.getExactCurrencyAmount()).isEqualTo(expected.getExactCurrencyAmount());
        assertThat(returned.getStatus()).isEqualTo(expected.getStatus());
        assertThat(returned.getRemittanceInformation().getType())
                .isEqualTo(expected.getRemittanceInformation().getType());
        assertThat(returned.getRemittanceInformation().getValue())
                .isEqualTo(expected.getRemittanceInformation().getValue());
    }
}
