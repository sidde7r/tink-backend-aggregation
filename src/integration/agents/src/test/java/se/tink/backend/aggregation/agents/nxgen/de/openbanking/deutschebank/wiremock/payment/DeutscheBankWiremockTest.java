package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank.wiremock.payment;

import java.time.LocalDate;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DeutscheBankWiremockTest {
    private static final String CONFIGURATION_FILE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/deutschebank/wiremock/payment/resources/configuration.yml";

    @Test
    public void testSepaPaymentInitiation() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen//de/openbanking/deutschebank/wiremock/payment/resources/sepa.aap";

        Payment payment = createRealDomesticPayment().build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-deutschebank-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCredentialField("username", "dummy_psu_id")
                        .withPayment(payment)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .buildWithoutLogin(PaymentCommand.class);
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testInstantSepaPaymentInitiation() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen//de/openbanking/deutschebank/wiremock/payment/resources/sepa_instant.aap";

        Payment payment =
                createRealDomesticPayment()
                        .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                        .build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-deutschebank-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCredentialField("username", "dummy_psu_id")
                        .withPayment(payment)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .buildWithoutLogin(PaymentCommand.class);
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    @Ignore // This won't work till we add recurring capability to Deutsche
    public void testRecurringPaymentInitiation() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen//de/openbanking/deutschebank/wiremock/payment/resources/recurring.aap";

        LocalDate refDate = LocalDate.of(2000, 02, 12);

        Payment payment =
                createRealDomesticPayment()
                        .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                        .withPaymentServiceType(PaymentServiceType.PERIODIC)
                        .withFrequency(Frequency.MONTHLY)
                        .withDayOfMonth(25)
                        .withStartDate(refDate.plusDays(4))
                        .withEndDate(refDate.plusMonths(2))
                        .withExecutionRule(ExecutionRule.FOLLOWING)
                        .build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-deutschebank-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCredentialField("username", "dummy_psu_id")
                        .withPayment(payment)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .buildWithoutLogin(PaymentCommand.class);
        agentWireMockPaymentTest.executePayment();
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("SepaReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("DE49500105177262199193");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "dummy owner");

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("DE78500105175814612478");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation);
    }
}
