package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.mock;

import static se.tink.libraries.enums.MarketCode.FI;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.mock.module.NordeaWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class NordeaFiPaymentWireMockTest {

    @Test
    public void testStandardPayment() throws Exception {

        // given
        final String configurationPath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/nordea/mock/resources/configuration.yml";
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/nordea/mock/resources/paymentStandardWireMock.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(FI, "fi-nordea-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(createStandardPayment())
                        .withHttpDebugTrace()
                        .addCallbackData("code", "dummyCode")
                        .withAgentModule(new NordeaWireMockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createStandardPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("610550873500157");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("FI0712424625986591");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "dummy owner");

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("FI4016726388543294");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(0.01);
        String currency = "EUR";

        return new Payment.Builder()
                .withUniqueId("232539fc-dfc3-47ed-8c70-205a928f05c8")
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .build();
    }

    @Test
    public void testRecurringPayment() throws Exception {

        // given
        final String configurationPath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/nordea/mock/resources/configuration.yml";
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/nordea/mock/resources/paymentRecurringWireMock.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(FI, "fi-nordea-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(createRecurringPayment())
                        .withHttpDebugTrace()
                        .addCallbackData("code", "dummyCode")
                        .withAgentModule(new NordeaWireMockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createRecurringPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("610550873500157");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("FI0712424625986591");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "dummy owner");

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("FI4016726388543294");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(0.01);
        String currency = "EUR";

        return new Payment.Builder()
                .withUniqueId("232539fc-dfc3-47ed-8c70-205a928f05c8")
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withStartDate(LocalDate.now().plus(1, ChronoUnit.DAYS))
                .withEndDate(LocalDate.now().plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.MONTHS))
                .withFrequency(Frequency.MONTHLY)
                .withExecutionDate(LocalDate.now().plus(1, ChronoUnit.DAYS))
                .build();
    }
}
