package se.tink.backend.aggregation.agents.legacy.banks.seb.mock;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.TransferCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.legacy.banks.seb.utilities.SEBDateUtil;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class SebSEAgentWireMockTest {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/legacy/banks/seb/mock/resources/";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";

    @Test
    public void testPayment() throws Exception {

        // given
        final String wireMockFilePath = RESOURCES_PATH + "seb-bankid-pis.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        BankGiroIdentifier destination = new BankGiroIdentifier("5768353");
        LocalDate executionDate = LocalDate.of(2020, 6, 22);
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "seb-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(
                                createPaymentWithExecutionDate(
                                        destination, "1047514784933", executionDate))
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .buildWithLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWithoutExecutionDate() throws Exception {
        // given
        SEBDateUtil.setClock(fixedClock("2021-09-21T03:30:00.00Z"));

        final String wireMockFilePath =
                RESOURCES_PATH + "seb-bankid-giro-without-execution-date.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        BankGiroIdentifier destination = new BankGiroIdentifier("5768353");
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "seb-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(createPaymentWithoutExecutionDate(destination, "50000038393"))
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .buildWithoutLogin(TransferCommand.class);

        // when
        agentWireMockPaymentTest.executePayment();

        // then
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWithFutureExecutionDate() throws Exception {
        // given
        SEBDateUtil.setClock(fixedClock("2021-09-21T03:30:00.00Z"));

        final String wireMockFilePath =
                RESOURCES_PATH + "seb-bankid-giro-future-execution-date.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        BankGiroIdentifier destination = new BankGiroIdentifier("5768353");
        LocalDate futureExecutionDate = LocalDate.of(2021, 10, 21);
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "seb-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(
                                createPaymentWithExecutionDate(
                                        destination, "50000038393", futureExecutionDate))
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .buildWithoutLogin(TransferCommand.class);

        // when
        agentWireMockPaymentTest.executePayment();

        // then
        Assert.assertTrue(true);
    }

    @Test
    public void testBankTransferToYourOwnAccountWithoutSigning() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "seb-bankid-pis-to-your-own-account.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        SwedishIdentifier destination = new SwedishIdentifier("52033350000");
        LocalDate executionDate = LocalDate.of(2021, 9, 22);
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "seb-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(
                                createBankTransferWithExecutionDate(
                                        destination, "50000038393", executionDate))
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .buildWithoutLogin(TransferCommand.class);

        // when
        agentWireMockPaymentTest.executePayment();

        // then
        Assert.assertTrue(true);
    }

    @Test
    public void testBankTransferToAnotherAccountWithoutExecutionDate() throws Exception {
        // given
        SEBDateUtil.setClock(fixedClock("2021-09-20T03:30:00.00Z"));

        final String wireMockFilePath =
                RESOURCES_PATH + "seb-bankid-pis-to-same-bank-without-execution-date.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        SwedishIdentifier destination = new SwedishIdentifier("52670267475");
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "seb-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(
                                createBankTransferWithoutExecutionDate(destination, "50000038393"))
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .buildWithoutLogin(TransferCommand.class);

        // when
        agentWireMockPaymentTest.executePayment();

        // then
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentSigningCancellation() throws Exception {
        // given
        SEBDateUtil.setClock(fixedClock("2021-09-20T03:30:00.00Z"));

        final String wireMockFilePath = RESOURCES_PATH + "seb-bankid-giro-cancellation.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        BankGiroIdentifier destination = new BankGiroIdentifier("5768353");
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "seb-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(createPaymentWithoutExecutionDate(destination, "50000038393"))
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .buildWithoutLogin(TransferCommand.class);

        // when
        ThrowingCallable throwingCallable = agentWireMockPaymentTest::executePayment;

        // then
        assertThatThrownBy(throwingCallable)
                .hasNoCause()
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage(
                        "Error when executing transfer: Du avbröt BankID. Vänligen försök igen.");
    }

    @Test
    public void testGiroPaymentDuplication() throws Exception {
        // given
        SEBDateUtil.setClock(fixedClock("2021-09-20T03:30:00.00Z"));

        final String wireMockFilePath = RESOURCES_PATH + "seb-bankid-giro-duplication.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        BankGiroIdentifier destination = new BankGiroIdentifier("5768353");
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "seb-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withTransfer(createPaymentWithoutExecutionDate(destination, "50000038393"))
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .buildWithoutLogin(TransferCommand.class);

        // when
        ThrowingCallable throwingCallable = agentWireMockPaymentTest::executePayment;

        // then
        assertThatThrownBy(throwingCallable)
                .hasNoCause()
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage(
                        "The payment could not be made because an identical payment is already registered");
    }

    @Test
    public void testRefresh() throws Exception {

        // given
        final String wireMockFilePath = RESOURCES_PATH + "seb_mock_refresh_log.aap";
        final String contractFilePath = RESOURCES_PATH + "agent-contract.json";

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

    private Clock fixedClock(String moment) {
        Instant instant = Instant.parse(moment);
        return Clock.fixed(instant, DEFAULT_ZONE_ID);
    }

    private Transfer createPaymentWithExecutionDate(
            AccountIdentifier destination, String remittanceInfo, LocalDate executionDate) {
        Transfer transfer = createPaymentWithoutExecutionDate(destination, remittanceInfo);
        transfer.setDueDate(Date.from(executionDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant()));

        return transfer;
    }

    private Transfer createPaymentWithoutExecutionDate(
            AccountIdentifier destination, String remittanceInfo) {
        Transfer transfer = createTransferWithoutExecutionDate(destination, remittanceInfo);
        transfer.setType(TransferType.PAYMENT);

        return transfer;
    }

    private Transfer createBankTransferWithExecutionDate(
            AccountIdentifier destination, String remittanceInfo, LocalDate executionDate) {
        Transfer transfer =
                createTransferWithExecutionDate(destination, remittanceInfo, executionDate);
        transfer.setType(TransferType.BANK_TRANSFER);

        return transfer;
    }

    private Transfer createBankTransferWithoutExecutionDate(
            AccountIdentifier destination, String remittanceInfo) {
        Transfer transfer = createTransferWithoutExecutionDate(destination, remittanceInfo);
        transfer.setType(TransferType.BANK_TRANSFER);

        return transfer;
    }

    private Transfer createTransferWithExecutionDate(
            AccountIdentifier destination, String remittanceInfo, LocalDate executionDate) {
        Transfer transfer = createTransferWithoutExecutionDate(destination, remittanceInfo);
        transfer.setDueDate(Date.from(executionDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant()));

        return transfer;
    }

    private Transfer createTransferWithoutExecutionDate(
            AccountIdentifier destination, String remittanceInfo) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(remittanceInfo);
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(328.0));
        transfer.setSource(new SwedishIdentifier("58398257466"));
        transfer.setSourceMessage("Tinkpay");
        transfer.setDestination(destination);
        transfer.setRemittanceInformation(remittanceInformation);

        return transfer;
    }
}
