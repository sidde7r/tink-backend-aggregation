package se.tink.backend.system.workers.processor.other.payment;

import org.junit.Test;
import se.tink.backend.core.Transaction;
import static org.assertj.core.api.Assertions.assertThat;

public class PaymentDetectionCommandTest {

    @Test
    public void testPaypalPaymentDetectionWithStar() throws Exception {

        Transaction transaction = new Transaction();

        transaction.setDescription(null);
        transaction.setOriginalDescription("PAYPAL *VICTORIA'S SECRET");

        PaymentDetectionCommand command = new PaymentDetectionCommand();

        command.execute(transaction);

        assertThat(transaction.getDescription()).isEqualToIgnoringCase("VICTORIA'S SECRET");
    }

    @Test
    public void testPaypalPaymentDetectionWithoutStar() throws Exception {

        Transaction transaction = new Transaction();

        transaction.setDescription(null);
        transaction.setOriginalDescription("PAYPAL VICTORIA'S SECRET");

        PaymentDetectionCommand command = new PaymentDetectionCommand();

        command.execute(transaction);

        assertThat(transaction.getDescription()).isEqualToIgnoringCase("VICTORIA'S SECRET");
    }

    @Test
    public void testPickDescriptionIfAvailable() throws Exception {

        Transaction transaction = new Transaction();

        transaction.setDescription("PAYPAL VICTORIA'S SECRET");
        transaction.setOriginalDescription("FOO BAR");

        PaymentDetectionCommand command = new PaymentDetectionCommand();

        command.execute(transaction);

        assertThat(transaction.getDescription()).isEqualToIgnoringCase("VICTORIA'S SECRET");
    }

}
