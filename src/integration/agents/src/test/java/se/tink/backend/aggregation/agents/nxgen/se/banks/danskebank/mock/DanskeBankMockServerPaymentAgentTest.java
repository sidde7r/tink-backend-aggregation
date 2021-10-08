package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.mock;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static se.tink.libraries.enums.MarketCode.SE;

import java.time.LocalDateTime;
import java.time.Month;
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

public class DanskeBankMockServerPaymentAgentTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources/configuration.yml";

    @Test
    public void testGiroPaymentWithExecutionDateEqualsNull() throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources/GiroPaymentExecutionEqualsNull.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "1200123456"));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifierType.SE_BG, "9008004"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(20));
        transfer.setType(TransferType.PAYMENT);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Donation");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setSourceMessage("Donation");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "se-danskebank-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWhereUserCancelsSigning() throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources/GiroPaymentWhereUserCancelsSigning.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "1200123456"));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifierType.SE_BG, "9008004"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(20));
        transfer.setType(TransferType.PAYMENT);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Donation");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setSourceMessage("Donation");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "se-danskebank-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        assertThatThrownBy(agentWireMockPaymentTest::executePayment)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage("You cancelled the BankID process. Please try again.");
    }

    @Test
    public void testGiroPaymentWithExecutionDateWhenFutureBusinessDate() throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources/GiroPaymentWithExecutionDateWhenFutureBusinessDate.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "1200123456"));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifierType.SE_PG, "9008004"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.PAYMENT);

        Date dueDate =
                Date.from(
                        LocalDateTime.of(2021, Month.NOVEMBER, 10, 00, 00, 00)
                                .atZone(ZoneId.of("CET"))
                                .toInstant());
        transfer.setDueDate(dueDate);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("test");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setSourceMessage("test");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "se-danskebank-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToAnotherBankWithExecutionDateEqualsNull() throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources/PaymentToAnotherBankWithExecutionDateEqualsNull.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "1200123456"));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifierType.SE, "9554123456"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.BANK_TRANSFER);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("test");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setSourceMessage("test");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "se-danskebank-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToAnotherPersonsAccountWithinSameBankWithExecutionDateEqualsNull()
            throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources/PaymentToAnotherPersonsAccountWithinSameBankWithExecutionDateEqualsNull.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "1200123456"));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifierType.SE, "1200654321"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.BANK_TRANSFER);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("test");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setSourceMessage("test");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "se-danskebank-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToUsersOwnAccountWithinSameBank() throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources/PaymentToUsersOwnAccountWithinSameBank.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "1200123456"));
        transfer.setDestination(AccountIdentifier.create(AccountIdentifierType.SE, "1200654321"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.BANK_TRANSFER);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("test");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setSourceMessage("test");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "se-danskebank-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

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
        Assert.assertTrue(true);
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
        Assert.assertTrue(true);
    }

    private Transfer createMockedDomesticTransfer() {
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "1200123456"));
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
        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.SE, "1200123456"));
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
