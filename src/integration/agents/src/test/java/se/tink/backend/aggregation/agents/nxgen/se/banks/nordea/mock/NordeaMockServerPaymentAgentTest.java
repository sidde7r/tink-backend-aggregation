package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.mock;

import static se.tink.libraries.enums.MarketCode.SE;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.TransferCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaMockServerPaymentAgentTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/configuration.yml";

    @Test
    public void testPayment() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordea-bankid-pis.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(createMockPayment())
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testOcrPayment() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordea-bankid_billecta.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(createOcrPayment())
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testAccountToAccountTransfer() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordea-bankid_sbab.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(createMockTransfer())
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testCancelledDueToDuplicationTransfer() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordea-bankid_cancel_duplication.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(createMockPayment())
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), TransferExecutionException.class);
            Assert.assertEquals(
                    "The payment could not be made because an identical payment is already registered",
                    e.getMessage());
        }
    }

    private Transfer createMockPayment() {
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "3300123456"));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifierType.SE_BG, "3228756"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(4246));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2020, 6, 22).atStartOfDay(ZoneId.of("CET")).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("610550873500157");
        transfer.setRemittanceInformation(remittanceInformation);

        return transfer;
    }

    private Transfer createMockTransfer() {
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "3300123456"));
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifierType.SE, "9252123456", "Tink Name"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(150000));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 02, 25).atStartOfDay(ZoneId.of("CET")).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("SBAB-BANK");
        transfer.setRemittanceInformation(remittanceInformation);

        return transfer;
    }

    private Transfer createOcrPayment() {
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "3300123456"));
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifierType.SE_BG, "4624292", "Tink Name"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(244.55));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 02, 24).atStartOfDay(ZoneId.of("CET")).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("124435959871558");
        transfer.setRemittanceInformation(remittanceInformation);

        return transfer;
    }
}
