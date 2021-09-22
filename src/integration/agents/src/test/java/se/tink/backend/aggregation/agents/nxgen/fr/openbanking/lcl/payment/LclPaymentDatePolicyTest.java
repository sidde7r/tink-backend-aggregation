package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class LclPaymentDatePolicyTest {

    private final LclPaymentDatePolicy lclPaymentDatePolicy = new LclPaymentDatePolicy();

    @Test
    public void returnProvidedExecutionDateIfNotNull() {
        LocalDate localDate = LocalDate.of(2021, 4, 17);
        Payment payment = new Payment.Builder().withExecutionDate(localDate).build();
        assertThat(lclPaymentDatePolicy.apply(payment)).isEqualTo(localDate);
    }

    @Test
    public void returnTodayExecutionDateIfFrenchOrMonacoIban() {
        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier("FR1420041010050500013M02606");
        Payment payment =
                new Payment.Builder().withDebtor(new Debtor(creditorAccountIdentifier)).build();
        assertThat(lclPaymentDatePolicy.apply(payment))
                .isEqualTo(lclPaymentDatePolicy.getCreationDate().toLocalDate());
    }

    @Test
    public void returnTodayExecutionDateIfInternationalIban() {
        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier("SE1420041010050500013M02606");
        Payment payment =
                new Payment.Builder().withCreditor(new Creditor(creditorAccountIdentifier)).build();
        assertThat(lclPaymentDatePolicy.apply(payment))
                .isEqualTo(lclPaymentDatePolicy.getCreationDate().toLocalDate());
    }
}
