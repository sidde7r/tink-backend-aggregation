package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class FiduciaPaymentMapperTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia/resources";

    private static final String TEST_BOUNDARY = "1234testBoundary4321";
    private static final String TEST_BIC = "TEST1234";

    private LocalDateTimeSource dateTimeSource = new ConstantLocalDateTimeSource();
    private FiduciaPaymentMapper mapper =
            new FiduciaPaymentMapper(new MockRandomValueGenerator(), dateTimeSource, TEST_BIC);

    @Test
    public void shouldTransformOneOffRequestCorrectly() {
        // given
        Payment payment =
                commonBuilder()
                        .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                        .withExecutionDate(dateTimeSource.now().toLocalDate())
                        .build();

        // when
        String paymentRequest = mapper.getPaymentRequest(payment);

        // then

        assertThat(paymentRequest).isXmlEqualTo(readAsStringFromFile("oneOffPaymentRequest.xml"));
    }

    @Test
    public void shouldTransformInstantPaymentCorrectly() {
        // This test is significant because there is a field in xml thats gets filled with today
        // date even in case of instant payment, when no execution date is really required
        // given
        Payment payment =
                commonBuilder()
                        .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                        .build();

        // when
        String paymentRequest = mapper.getPaymentRequest(payment);

        // then

        assertThat(paymentRequest).isXmlEqualTo(readAsStringFromFile("oneOffPaymentRequest.xml"));
    }

    @Test
    public void shouldTransformRecurringRequestCorrectly() {
        // given
        Payment payment =
                commonBuilder()
                        .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                        .withPaymentServiceType(PaymentServiceType.PERIODIC)
                        .withFrequency(Frequency.WEEKLY)
                        .withStartDate(dateTimeSource.now().toLocalDate().plusDays(1))
                        .withEndDate(dateTimeSource.now().toLocalDate().plusWeeks(1).plusDays(2))
                        .withExecutionRule(ExecutionRule.FOLLOWING)
                        .withDayOfWeek(DayOfWeek.SATURDAY)
                        .build();

        FiduciaPaymentMapper mapper =
                new FiduciaPaymentMapper(
                        new MockRandomValueGenerator(),
                        new ConstantLocalDateTimeSource(),
                        TEST_BIC);
        // when
        String paymentRequest = mapper.getRecurringPaymentRequest(payment, TEST_BOUNDARY);

        // then
        assertThat(paymentRequest)
                .isEqualToNormalizingNewlines(readAsStringFromFile("recurringPayment.txt"));
    }

    private Payment.Builder commonBuilder() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Super awesome test remittance information 123");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("DE53500105177116438385");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("DE43500105171537295773");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(0.99);

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(amount.getCurrencyCode())
                .withRemittanceInformation(remittanceInformation);
    }

    private static String readAsStringFromFile(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(TEST_DATA_PATH, filename)));
        } catch (IOException e) {
            return null;
        }
    }
}
