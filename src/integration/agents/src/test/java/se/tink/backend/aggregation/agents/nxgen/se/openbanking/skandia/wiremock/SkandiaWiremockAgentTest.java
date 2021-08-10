package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.wiremock;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SkandiaWiremockAgentTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/skandia/wiremock/resources/configuration.yml";

    @Test
    public void testRefresh() throws Exception {
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/skandia/wiremock/resources/skandia_ob_wiremock.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/skandia/wiremock/resources/agent-contract.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.SE, "se-skandiabanken-ob", wireMockServerFilePath)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("code", "dummyCode")
                        .withConfigurationFile(configuration)
                        .addRefreshableItems()
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        agentWireMockRefreshTest.executeRefresh();

        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testDomesticCreditTransfersPayments() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/skandia/wiremock/resources/skandia_ob_domestic_payment.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("test");

        final Creditor to =
                new Creditor(
                        AccountIdentifier.create(AccountIdentifierType.SE, "12345678900"), "Tink");
        final Debtor from =
                new Debtor(AccountIdentifier.create(AccountIdentifierType.SE, "12345654321"));

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-skandiabanken-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "dummyCode")
                        .withPayment(createMockedDomesticPayment(remittanceInformation, to, from))
                        .buildWithLogin(PaymentCommand.class);

        // when / then (execution and assertion currently done in the same step)
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testBankGiroDomesticCreditTransfersPaymentsWithMessage() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/skandia/wiremock/resources/skandia_ob_bank_giro_domestic_payment_with_message.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("PeaceAndLove");

        final Creditor to =
                new Creditor(
                        AccountIdentifier.create(AccountIdentifierType.SE_BG, "11111111"), "Tink");
        final Debtor from =
                new Debtor(AccountIdentifier.create(AccountIdentifierType.SE, "12345654321"));

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-skandiabanken-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "dummyCode")
                        .withPayment(createMockedDomesticPayment(remittanceInformation, to, from))
                        .buildWithLogin(PaymentCommand.class);

        // when / then (execution and assertion currently done in the same step)
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testBankGiroDomesticCreditTransfersPayments() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/skandia/wiremock/resources/skandia_ob_bank_giro_domestic_payment.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("11111111111111111");

        final Creditor to =
                new Creditor(
                        AccountIdentifier.create(AccountIdentifierType.SE_BG, "11111111"), "Tink");
        final Debtor from =
                new Debtor(AccountIdentifier.create(AccountIdentifierType.SE, "12345654321"));

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-skandiabanken-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "dummyCode")
                        .withPayment(createMockedDomesticPayment(remittanceInformation, to, from))
                        .buildWithLogin(PaymentCommand.class);

        // when / then (execution and assertion currently done in the same step)
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testPlusGiroDomesticCreditTransfersPayments() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/skandia/wiremock/resources/skandia_ob_plus_giro_domestic_payment.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("11111111111111111");

        final Creditor to =
                new Creditor(
                        AccountIdentifier.create(AccountIdentifierType.SE_PG, "11111111"), "Tink");
        final Debtor from =
                new Debtor(AccountIdentifier.create(AccountIdentifierType.SE, "12345654321"));

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-skandiabanken-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "dummyCode")
                        .withPayment(createMockedDomesticPayment(remittanceInformation, to, from))
                        .buildWithLogin(PaymentCommand.class);

        // when / then (execution and assertion currently done in the same step)
        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment(
            RemittanceInformation remittanceInformation, Creditor to, Debtor from) {
        String currency = "SEK";
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", currency);

        return new Payment.Builder()
                .withCreditor(to)
                .withDebtor(from)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId("915085259414190")
                .withExecutionDate(LocalDate.parse("2021-06-01"))
                .build();
    }
}
