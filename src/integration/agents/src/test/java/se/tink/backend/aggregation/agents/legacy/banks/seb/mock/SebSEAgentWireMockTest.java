package se.tink.backend.aggregation.agents.legacy.banks.seb.mock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.TransferCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class SebSEAgentWireMockTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/seb/mock/resources/configuration.yml";

    @Test
    public void testPayment() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/seb/mock/resources/seb-bankid-pis.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "seb-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(createTransfer())
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .buildWithLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    private Transfer createTransfer() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("1047514784933");
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(328.0));
        transfer.setSource(new SwedishIdentifier("58398257466"));
        transfer.setSourceMessage("Tinkpay");
        transfer.setDestination(new BankGiroIdentifier("5768353"));
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2020, 6, 22).atStartOfDay(ZoneId.of("CET")).toInstant()));

        return transfer;
    }

    @Test
    public void testRefresh() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/seb/mock/resources/seb_mock_refresh_log.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/seb/mock/resources/agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.SE)
                        .withProviderName("seb-bankid")
                        .withWireMockFilePath(wireMockFilePath)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "199312050231")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
