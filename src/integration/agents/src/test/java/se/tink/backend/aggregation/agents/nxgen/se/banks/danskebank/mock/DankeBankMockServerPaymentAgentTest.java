package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.mock;

import static se.tink.libraries.enums.MarketCode.SE;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.TransferCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

@Ignore
public class DankeBankMockServerPaymentAgentTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources/configuration.yml";

    @Test
    public void testPayment() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources/se-danskebank-bankid-pis.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "se-danskebank-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(createMockedDomesticTransfer())
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testPaymentWithNonNullDueDate() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources/se-danskebank-bankid-pis.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "se-danskebank-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(createMockedNonNullDueDatePayment())
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Transfer createMockedDomesticTransfer() {
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "3300123456"));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifierType.SE_BG, "5961111"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(7138));
        transfer.setType(TransferType.PAYMENT);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("610550873500157");
        transfer.setRemittanceInformation(remittanceInformation);
        return transfer;
    }

    private Transfer createMockedNonNullDueDatePayment() {
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "3300123456"));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifierType.SE_BG, "5961111"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(7138d));
        transfer.setType(TransferType.PAYMENT);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("610550873500157");
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setDueDate(getNonNullDueDate());
        return transfer;
    }

    private Date getNonNullDueDate() {
        return Date.from(
                LocalDateTime.of(2020, Month.OCTOBER, 28, 00, 00, 00)
                        .atZone(ZoneId.of("CET"))
                        .toInstant());
    }
}
