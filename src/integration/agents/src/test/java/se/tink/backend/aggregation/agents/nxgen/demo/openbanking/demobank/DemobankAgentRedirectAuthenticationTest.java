package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DemobankAgentRedirectAuthenticationTest {

    private static final String SOURCE_IDENTIFIER = "";
    private static final String DESTINATION_IDENTIFIER = "";

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("it", "it-demobank-open-banking-redirect")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setRedirectUrl(
                                "https://127.0.0.1:7357/api/v1/credentials/third-party/callback")
                        .expectLoggedIn(false)
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPaymentUKOB(createMockedDomesticPayment());
    }

    @Test
    public void testRecurringPayment() throws Exception {
        builder.build().testGenericPaymentUKOB(createMockedRecurringDomesticPayment());
    }

    private Payment createMockedDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("0.1", "EUR");
        LocalDate executionDate = LocalDate.now();
        String currency = "EUR";

        return new Payment.Builder()
                .withPaymentServiceType(PaymentServiceType.SINGLE)
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifier.Type.IBAN, DESTINATION_IDENTIFIER),
                                "Unknown person"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifier.Type.IBAN, SOURCE_IDENTIFIER)))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "Message"))
                .withUniqueId(RandomUtils.generateRandomHexEncoded(15))
                .build();
    }

    private Payment createMockedRecurringDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("0.3", "EUR");
        LocalDate executionDate = LocalDate.now();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(1);
        String currency = "EUR";

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Periodic payment");
        remittanceInformation.setType(RemittanceInformationType.REFERENCE);

        return new Payment.Builder()
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifier.Type.IBAN, DESTINATION_IDENTIFIER),
                                "Unknown person"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifier.Type.IBAN, SOURCE_IDENTIFIER)))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withFrequency(Frequency.WEEKLY)
                .withStartDate(startDate)
                .withEndDate(endDate)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId(RandomUtils.generateRandomHexEncoded(15))
                .build();
    }
}
