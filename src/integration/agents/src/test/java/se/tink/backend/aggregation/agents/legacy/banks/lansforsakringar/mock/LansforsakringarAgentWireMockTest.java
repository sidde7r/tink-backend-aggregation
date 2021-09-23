package se.tink.backend.aggregation.agents.banks.lansforsakringar.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.LansforsakringarDateUtil;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
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

public class LansforsakringarAgentWireMockTest {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/configuration.yml";

    @Test
    public void testRefresh() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/lansforsakringar-all-items-refresh.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/agent-contract.json";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.SE)
                        .withProviderName("lansforsakringar-bankid")
                        .withWireMockFilePath(wireMockFilePath)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "199909091234")
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
        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("90247744574"));
        transfer.setDestination(new BankGiroIdentifier("7355837"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(171d));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2020, 6, 17).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("108117405510002");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/lansforsakringar-bankid-pis.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "lansforsakringar-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWithExecutionDateEqualsNull() throws Exception {
        // given
        LansforsakringarDateUtil.setClock(fixedClock("2021-09-17T08:51:58.622Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("12345678912"));
        transfer.setDestination(new BankGiroIdentifier("1202407"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.PAYMENT);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("TEXT");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/lansforsakringarGiroPaymentWithExecutionDateEqualsNull.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "lansforsakringar-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWithExecutionDateWhenFutureBusinessDate() throws Exception {
        // given
        LansforsakringarDateUtil.setClock(fixedClock("2021-09-21T09:02:26.00Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("12345678912"));
        transfer.setDestination(new BankGiroIdentifier("1202407"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 9, 23).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("TEXT");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/lansforsakringarGiroPaymentWithExecutionDateWhenFutureBusinessDate.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "lansforsakringar-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToUsersOwnAccountWithinSameBank() throws Exception {
        // given
        LansforsakringarDateUtil.setClock(fixedClock("2021-09-21T09:16:00.00Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("12345678912"));
        transfer.setDestination(new SwedishIdentifier("12345678934"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSourceMessage("SOURCE_TEXT");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("TEXT");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/lansforsakringarPaymentToUsersOwnAccountWithinSameBank.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "lansforsakringar-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToAnotherPersonsAccountWithinSameBankWithExecutionDateEqualsNull()
            throws Exception {
        // given
        LansforsakringarDateUtil.setClock(fixedClock("2021-09-21T13:19:46.00Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("12345678912"));
        transfer.setDestination(new SwedishIdentifier("90254080393"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSourceMessage("");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("tinkTest");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/lansforsakringarPaymentToAnotherPersonsAccountWithinSameBankWithExecutionDateEqualsNull.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "lansforsakringar-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToAnotherBankWithExecutionDateEqualsNull() throws Exception {
        // given
        LansforsakringarDateUtil.setClock(fixedClock("2021-09-21T11:50:01.00Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("12345678912"));
        transfer.setDestination(new SwedishIdentifier("818181818181818"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(2));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSourceMessage("");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("TEXT");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/lansforsakringarPaymentToAnotherBankWithExecutionDateEqualsNull.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "lansforsakringar-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWhereUserCancelSigningOfPayment() throws Exception {
        // given
        LansforsakringarDateUtil.setClock(fixedClock("2021-09-21T12:28:16.00Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("12345678912"));
        transfer.setDestination(new BankGiroIdentifier("1202407"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 9, 23).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("TEXT");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar/mock/resources/lansforsakringarGiroPaymentWhereUserCancelSigningOfPayment.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "lansforsakringar-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (BankIdException e) {
            Assert.assertEquals("Cause: BankIdError.CANCELLED", e.getMessage());
        }
    }

    private Clock fixedClock(String moment) {
        return Clock.fixed(Instant.parse(moment), DEFAULT_ZONE_ID);
    }
}
