package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.mock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.TransferCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankSEAgentWireMockTest {

    private final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/configuration.yml";

    @Test
    public void testRefreshAllAccounts() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbank-bankid-all-accounts.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/agent-contract-all-accounts.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(MarketCode.SE, "swedbank-bankid", wireMockFilePath)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withConfigurationFile(configuration)
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
    public void testRefresh() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbank_mock_refresh_log.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/agent-contract.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(MarketCode.SE, "swedbank-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
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
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbank-bankid-pis.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "swedbank-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .addTransfer(createMockedDomesticTransfer())
                        .buildWithLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    /**
     * In the bug report https://tinkab.atlassian.net/browse/PAY1-674 The flow crashed due to user
     * not authorised register payee After the fix, due to the origin S3 log did not really go to
     * the very end There will still be exception.
     */
    @Test
    public void testPaymentWithExtendedBankIdUsageAndMultiBankProfiles() throws Exception {
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbank_pis_extended_bankid.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "savingsbank-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .addTransfer(createMockedDomesticTransfer())
                        .buildWithLogin(TransferCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (TransferExecutionException e) {
            Assert.assertEquals("Could not get recipient name from user", e.getMessage());
            Assert.assertNotEquals(
                    "Activation of extended use for BankId required", e.getMessage());
        }
    }

    private Transfer createMockedDomesticTransfer() {

        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifier.Type.SE, "832791234567890"));

        transfer.setDestination(AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "1202407"));
        transfer.setAmount(Amount.inSEK(856d));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(
                        LocalDate.of(2020, 6, 17)
                                .atStartOfDay(ZoneId.of("Europe/Stockholm"))
                                .toInstant()));
        transfer.setDestinationMessage("642257434400156");

        return transfer;
    }
}
