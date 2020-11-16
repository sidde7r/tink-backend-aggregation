package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.mock;

import static se.tink.libraries.enums.MarketCode.SE;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.TransferCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

@Ignore
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
                        .addTransfer(createMockedDomesticTransfer())
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Transfer createMockedDomesticTransfer() {
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifier.Type.SE, "3300123456"));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "3228756"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(4246));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2020, 6, 22).atStartOfDay(ZoneId.of("CET")).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("610550873500157");
        transfer.setRemittanceInformation(remittanceInformation);

        return transfer;
    }
}
