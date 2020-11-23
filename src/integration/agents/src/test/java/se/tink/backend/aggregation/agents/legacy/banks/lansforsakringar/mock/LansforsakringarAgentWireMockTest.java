package se.tink.backend.aggregation.agents.banks.lansforsakringar.mock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.TransferCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class LansforsakringarAgentWireMockTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/configuration.yml";

    @Test
    public void testPayment() throws Exception {
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/lansforsakringar-bankid-pis.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "lansforsakringar-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .addTransfer(createMockedDomesticTransfer())
                        .buildWithLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Transfer createMockedDomesticTransfer() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("108117405510002");
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifier.Type.SE, "90247744574"));

        transfer.setDestination(AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "7355837"));
        transfer.setAmount(Amount.inSEK(171d));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(
                        LocalDate.of(2020, 6, 17)
                                .atStartOfDay(ZoneId.of("Europe/Stockholm"))
                                .toInstant()));
        transfer.setRemittanceInformation(remittanceInformation);

        return transfer;
    }
}
