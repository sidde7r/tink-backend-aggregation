package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.wiremock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class SbabWireMockTest {

    private static final String PSU = "psu";
    private static final String configurationPath =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/sbab/wiremock/resources/configuration.yml";

    @Test
    public void testRefresh() throws Exception {

        // given
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/sbab/wiremock/resources/wireMock.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/sbab/wiremock/resources/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.SE, "se-sbab-ob", wireMockServerFilePath)
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), PSU)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .withConfigurationFile(configuration)
                        .addRefreshableItems()
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testPayment() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/sbab/wiremock/resources/se-sbab-ob-transfer_successful.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "se-sbab-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(createMockedDomesticPayment())
                        .withHttpDebugTrace()
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), PSU)
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", "SEK");
        String currency = "SEK";
        return new Payment.Builder()
                .withCreditor(new Creditor(new SwedishIdentifier("52871234567"), "Recipient Name"))
                .withExactCurrencyAmount(amount)
                .withDebtor(new Debtor(new SwedishIdentifier("3300123456")))
                .withCurrency(currency)
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "Rr1234"))
                .build();
    }
}
