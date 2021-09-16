package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.mock;

import static se.tink.libraries.enums.MarketCode.SE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.utilities.NordeaDateUtil;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaYellowPaymentTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/configuration.yml";

    @Test
    public void testGiroPaymentWithExecutionDateEqualsNull() throws Exception {
        reflectTheClock("2021-09-20T07:15:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordeaGiroPaymentExecutionEqualsNull.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("3300123456"));
        transfer.setDestination(new BankGiroIdentifier("51225860"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.11d));
        transfer.setType(TransferType.PAYMENT);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("54356060027588277");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWithExecutionDateWhenFutureBusinessDate() throws Exception {
        reflectTheClock("2021-09-20T09:15:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordeaGiroPaymentWithExecutionDateWhenFutureBusinessDate.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("3265123456"));
        transfer.setDestination(new BankGiroIdentifier("51225860"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.11d));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2022, 2, 2).atStartOfDay(ZoneId.of("CET")).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("54356060027588277");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToUsersOwnAccountWithinSameBank() throws Exception {
        reflectTheClock("2021-09-19T19:15:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordeaPaymentToUsersOwnAccountWithinSameBank.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("3265123456"));
        transfer.setDestination(new SwedishIdentifier("3265654321"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.11d));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDestinationMessage("SH");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("54356060027588277");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
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
        reflectTheClock("2021-09-20T07:15:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordeaPaymentToAnotherPersonsAccountWithinSameBankWithExecutionDateEqualsNull.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("3265123456"));
        AccountIdentifier des =
                AccountIdentifier.create(AccountIdentifierType.SE, "30600265722", "SH");
        transfer.setDestination(des);
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.01d));
        transfer.setType(TransferType.BANK_TRANSFER);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("sai.he:test");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToAnotherBankWithExecutionDateEqualsNull() throws Exception {
        reflectTheClock("2021-09-19T07:15:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordeaPaymentToAnotherBankWithExecutionDateEqualsNull.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("3265123456"));
        transfer.setDestination(new SwedishIdentifier("9552123456"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.11d));
        transfer.setType(TransferType.BANK_TRANSFER);

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("in");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWhereUserCancelSigningOfPayment() throws Exception {
        reflectTheClock("2021-09-19T19:15:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordeaGiroPaymentWhereUserCancelSigningOfPayment.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("3265123456"));
        transfer.setDestination(new BankGiroIdentifier("51225860"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.11d));
        transfer.setType(TransferType.PAYMENT);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("54356060027588277");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (TransferExecutionException e) {
            Assert.assertEquals(
                    "You cancelled the BankID process. Please try again.", e.getMessage());
        }
    }

    @Test
    public void testGiroPaymentWithReferenceLengthXplusOne() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordeaGiroPaymentWithReferenceLengthXplusOne.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("3265123456"));
        transfer.setDestination(new BankGiroIdentifier("51225860"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.59d));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2022, 6, 3).atStartOfDay(ZoneId.of("CET")).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("1234567891234");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (TransferExecutionException e) {
            Assert.assertEquals("Error in reference number (OCR)", e.getMessage());
        }
    }

    @Test
    public void testA2APaymentWithReferenceLengthXplusOne() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/mock/resources/nordeaA2APaymentWithReferenceLengthXplusOne.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("3265123456"));
        transfer.setDestination(new SwedishIdentifier("3300123456"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.59d));
        transfer.setType(TransferType.BANK_TRANSFER);

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("123456789123456");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(SE, "nordea-bankid", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(TransferCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (TransferMessageException e) {
            Assert.assertTrue(
                    e.getMessage().startsWith("Too long destination message set for transfer"));
        }
    }

    private void reflectTheClock(String datetime)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Clock clock = Clock.fixed(Instant.parse(datetime), ZoneId.of("CET"));
        Method m = NordeaDateUtil.class.getDeclaredMethod("setClockForTesting", Clock.class);
        m.setAccessible(true);
        m.invoke(null, clock);
    }
}
