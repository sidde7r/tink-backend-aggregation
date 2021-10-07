package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.mock;

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
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.utilities.HandelsbankenDateUtils;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenBankIdYellowWiremockTest {

    @Test
    public void testGiroPaymentWithExecutionDateEqualsNull() throws Exception {
        reflectTheClock("2021-09-27T15:34:30Z");
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/handelsbanken/mock/resources/shbGiroPaymentExecutionEqualsNull.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("6410123456"));
        transfer.setDestination(new BankGiroIdentifier("51225860"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.11d));
        transfer.setType(TransferType.PAYMENT);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("54356060027588277");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "handelsbanken-bankid", wireMockFilePath)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWithExecutionDateWhenFutureBusinessDate() throws Exception {
        reflectTheClock("2021-09-27T15:36:30Z");
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/handelsbanken/mock/resources/shbGiroPaymentWithExecutionDateWhenFutureBusinessDate.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("6410123456"));
        transfer.setDestination(new BankGiroIdentifier("51225860"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.11d));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 9, 30).atStartOfDay(ZoneId.of("CET")).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("54356060027588277");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "handelsbanken-bankid", wireMockFilePath)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToUsersOwnAccountWithinSameBank() throws Exception {
        reflectTheClock("2021-09-27T15:38:30Z");
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/handelsbanken/mock/resources/shbPaymentToUsersOwnAccountWithinSameBank.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("6410123456"));
        transfer.setSourceMessage("");
        transfer.setDestination(new SwedishIdentifier("6410654321"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.11d));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDestinationMessage("SH");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Test");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "handelsbanken-bankid", wireMockFilePath)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToAnotherBankWithExecutionDateEqualsNull() throws Exception {
        reflectTheClock("2021-09-27T15:42:30Z");
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/handelsbanken/mock/resources/shbPaymentToAnotherBankWithExecutionDateEqualsNull.aap";

        Transfer transfer = new Transfer();
        transfer.setSourceMessage("");
        transfer.setSource(new SwedishIdentifier("6410123456"));
        transfer.setDestination(new SwedishIdentifier("9552123456"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.11d));
        transfer.setType(TransferType.BANK_TRANSFER);

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Tink Test");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "handelsbanken-bankid", wireMockFilePath)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWhereUserCancelSigningOfPayment() throws Exception {
        reflectTheClock("2021-09-28T07:59:30Z");
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/handelsbanken/mock/resources/shbGiroPaymentWhereUserCancelSigningOfPayment.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("3265123456"));
        transfer.setDestination(new BankGiroIdentifier("51225860"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.11d));
        transfer.setType(TransferType.PAYMENT);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("54356060027588277");
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "handelsbanken-bankid", wireMockFilePath)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithLogin(TransferCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (TransferExecutionException e) {
            Assert.assertEquals("Du avbröt BankID. Vänligen försök igen.", e.getMessage());
            return;
        }
        Assert.fail();
    }

    @Test
    public void testGiroPaymentWithReferenceLengthXplusOne() throws Exception {
        reflectTheClock("2021-09-27T15:44:30Z");
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/handelsbanken/mock/resources/shbGiroPaymentWithReferenceLengthXplusOne.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("6410123456"));
        transfer.setDestination(new BankGiroIdentifier("51225860"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.11d));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 9, 30).atStartOfDay(ZoneId.of("CET")).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("54356060027588277543560600275");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "handelsbanken-bankid", wireMockFilePath)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithLogin(TransferCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (TransferExecutionException e) {
            Assert.assertEquals(
                    "Du har angivit OCR-numret på ett felaktigt sätt. Numret finns längst ner till vänster på avin, mellan två #-tecken.Var vänlig och ange samtliga siffror i en följd. Endast siffrorna ska anges.",
                    e.getMessage());
            return;
        }
        Assert.fail();
    }

    @Test
    public void testA2APaymentWithReferenceLengthXplusOne() throws Exception {
        reflectTheClock("2021-09-27T15:46:30Z");
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/handelsbanken/mock/resources/shbA2APaymentWithReferenceLengthXplusOne.aap";

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("6410123456"));
        transfer.setDestination(new SwedishIdentifier("9554123456"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.59d));
        transfer.setType(TransferType.BANK_TRANSFER);

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("123456789123456");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setRemittanceInformation(remittanceInformation);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "handelsbanken-bankid", wireMockFilePath)
                        .withTransfer(transfer)
                        .withHttpDebugTrace()
                        .buildWithLogin(TransferCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (TransferMessageException e) {
            Assert.assertTrue(
                    e.getMessage().startsWith("Too long destination message set for transfer"));
            return;
        }
        Assert.fail();
    }

    private void reflectTheClock(String datetime)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Clock clock = Clock.fixed(Instant.parse(datetime), ZoneId.of("CET"));
        Method m =
                HandelsbankenDateUtils.class.getDeclaredMethod("setClockForTesting", Clock.class);
        m.setAccessible(true);
        m.invoke(null, clock);
    }
}
