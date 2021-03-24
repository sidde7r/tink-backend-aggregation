package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.wiremock;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class LansforsakringarPaymentWiremockTest {
    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/configuration.yml";

    @Test
    public void testExternalBankTransfer() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/lansforsakringarExternalBankTransfer.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-lansforsakringar-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "dummyCode")
                        .withPayment(
                                createMockedPayment(
                                        getSeCreditor("33820000000"),
                                        RemittanceInformationUtils
                                                .generateUnstructuredRemittanceInformation(
                                                        "Remmitance")))
                        .withHttpDebugTrace()
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testInternalBankTransfer() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/lansforsakringarInternalBankTransfer.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-lansforsakringar-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "dummyCode")
                        .withPayment(
                                createMockedPayment(
                                        getSeCreditor("90257654321"),
                                        RemittanceInformationUtils
                                                .generateUnstructuredRemittanceInformation(
                                                        "Remmitance")))
                        .withHttpDebugTrace()
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testBgPaymentWithOCR() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/lansforsakringarBankgiroWithOcr.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-lansforsakringar-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "dummyCode")
                        .withPayment(
                                createMockedPayment(
                                        getSeBgCreditor(),
                                        RemittanceInformationUtils
                                                .generateStructuredRemittanceInformationWithOCR(
                                                        "33001227314")))
                        .withHttpDebugTrace()
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testBgPaymentWithMessage() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/lansforsakringarBankgiroWithMessage.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-lansforsakringar-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "dummyCode")
                        .withPayment(
                                createMockedPayment(
                                        getSeBgCreditor(),
                                        RemittanceInformationUtils
                                                .generateUnstructuredRemittanceInformation(
                                                        "Remmitance")))
                        .withHttpDebugTrace()
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedPayment(
            Creditor creditor, RemittanceInformation remittanceInformation) {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", "SEK");
        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(AccountIdentifierType.SE, "90251234567")))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(LocalDate.parse("2021-01-21"))
                .withCurrency("SEK")
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId("")
                .build();
    }

    private Creditor getSeCreditor(String accountNumber) {
        return new Creditor(
                AccountIdentifier.create(AccountIdentifierType.SE, accountNumber),
                "Recipient Name");
    }

    private Creditor getSeBgCreditor() {
        return new Creditor(
                AccountIdentifier.create(AccountIdentifierType.SE_BG, "991-2148"),
                "Recipient Name");
    }
}
