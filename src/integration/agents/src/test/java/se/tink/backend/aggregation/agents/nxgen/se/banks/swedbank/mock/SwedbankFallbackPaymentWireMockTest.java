package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.TransferCommand;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.mock.module.SwedbankFallbackWireMockTestModule;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankFallbackPaymentWireMockTest {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/configuration_payments.yml";

    @Test
    public void testGiroPaymentWithExecutionDateEqualsNull() throws Exception {
        // given
        SwedbankDateUtils.setClock(fixedClock("2021-09-22T15:50:22Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("816611000000000"));
        transfer.setDestination(new BankGiroIdentifier("9008004"));
        setTransferParameters(transfer, TransferType.PAYMENT);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbankGiroPaymentWithExecutionDateEqualsNull.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-swedbank-fallback", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new SwedbankFallbackWireMockTestModule())
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
        SwedbankDateUtils.setClock(fixedClock("2021-09-22T15:54:10Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("816611000000000"));
        transfer.setDestination(new BankGiroIdentifier("9008004"));
        setTransferParameters(transfer, TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 9, 24).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbankGiroPaymentWithExecutionDateWhenFutureBusinessDate.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-swedbank-fallback", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new SwedbankFallbackWireMockTestModule())
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
        SwedbankDateUtils.setClock(fixedClock("2021-09-23T09:41:00Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("816611000000000"));
        transfer.setDestination(new SwedishIdentifier("816612000000000"));
        setTransferParameters(transfer, TransferType.BANK_TRANSFER);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 9, 24).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbankPaymentToUsersOwnAccountWithinSameBank.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-swedbank-fallback", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new SwedbankFallbackWireMockTestModule())
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
        SwedbankDateUtils.setClock(fixedClock("2021-09-23T15:41:28Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("816612000000000"));
        transfer.setDestination(new SwedishIdentifier("832797000000000"));
        setTransferParameters(transfer, TransferType.BANK_TRANSFER);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbankPaymentToAnotherPersonsAccountWithinSameBankWithExecutionDateEqualsNull.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-swedbank-fallback", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new SwedbankFallbackWireMockTestModule())
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
        SwedbankDateUtils.setClock(fixedClock("2021-09-23T09:46:48Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("816611000000000"));
        transfer.setDestination(new SwedishIdentifier("33820004238"));
        setTransferParameters(transfer, TransferType.BANK_TRANSFER);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbankPaymentToAnotherBankWithExecutionDateEqualsNull.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-swedbank-fallback", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new SwedbankFallbackWireMockTestModule())
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentToNewRecipient() throws Exception {
        // given
        SwedbankDateUtils.setClock(fixedClock("2021-09-24T15:15:45Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("816612000000000"));
        AccountIdentifier identifier =
                AccountIdentifier.create(AccountIdentifierType.SE_BG, "9008004", "redcross");
        transfer.setDestination(identifier);
        setTransferParameters(transfer, TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 9, 28).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbankGiroPaymentToNewRecipient.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-swedbank-fallback", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new SwedbankFallbackWireMockTestModule())
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentToExistingRecipient() throws Exception {
        // given
        SwedbankDateUtils.setClock(fixedClock("2021-09-24T07:17:55Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("816612000000000"));
        transfer.setDestination(new BankGiroIdentifier("9008004"));
        setTransferParameters(transfer, TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 9, 28).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbankGiroPaymentToExistingRecipient.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-swedbank-fallback", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new SwedbankFallbackWireMockTestModule())
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testA2APaymentToNewRecipient() throws Exception {
        // given
        SwedbankDateUtils.setClock(fixedClock("2021-09-24T15:27:44Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("816612000000000"));
        AccountIdentifier identifier =
                AccountIdentifier.create(AccountIdentifierType.SE, "33820004238", "redcross");
        transfer.setDestination(identifier);
        setTransferParameters(transfer, TransferType.BANK_TRANSFER);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 9, 28).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbankA2APaymentToNewRecipient.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-swedbank-fallback", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new SwedbankFallbackWireMockTestModule())
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testA2APaymentToExistingRecipient() throws Exception {
        // given
        SwedbankDateUtils.setClock(fixedClock("2021-09-23T09:46:48Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("816611000000000"));
        transfer.setDestination(new SwedishIdentifier("33820004238"));
        setTransferParameters(transfer, TransferType.BANK_TRANSFER);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbankA2APaymentToExistingRecipient.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-swedbank-fallback", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new SwedbankFallbackWireMockTestModule())
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testDomesticPaymentWhereUserCancelSigningOfPayment() throws Exception {
        // given
        SwedbankDateUtils.setClock(fixedClock("2021-09-23T09:50:46Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("816611000000000"));
        transfer.setDestination(new SwedishIdentifier("33820004238"));
        setTransferParameters(transfer, TransferType.BANK_TRANSFER);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbankDomesticPaymentWhereUserCancelSigningOfPayment.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-swedbank-fallback", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new SwedbankFallbackWireMockTestModule())
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (TransferExecutionException e) {
            Assert.assertEquals("Could not confirm transfer with BankID signing.", e.getMessage());
        }
    }

    @Test
    public void testDuplicateGiroPaymentWithSameExecutionDate() throws Exception {
        // given
        SwedbankDateUtils.setClock(fixedClock("2021-09-24T07:20:56Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("816612000000000"));
        transfer.setDestination(new BankGiroIdentifier("9008004"));
        setTransferParameters(transfer, TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 9, 28).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank/mock/resources/swedbankDuplicateGiroPaymentWithSameExecutionDate.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-swedbank-fallback", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new SwedbankFallbackWireMockTestModule())
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        // when then
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (TransferExecutionException e) {
            Assert.assertEquals(
                    "The payment could not be made because an identical payment is already registered",
                    e.getMessage());
        }
    }

    private void setTransferParameters(Transfer transfer, TransferType transferType) {
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(transferType);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("abc");
        transfer.setRemittanceInformation(remittanceInformation);
    }

    private Clock fixedClock(String moment) {
        return Clock.fixed(Instant.parse(moment), DEFAULT_ZONE_ID);
    }
}
