package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.wiremock;

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
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenExecutorUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class IcaBankenSePaymentWireMockTest {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");

    @Test
    public void testGiroPaymentWithExecutionDateEqualsNull() throws Exception {
        // given
        IcaBankenExecutorUtils.setClock(fixedClock("2021-09-21T15:21:16Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("92732490005"));
        transfer.setDestination(new BankGiroIdentifier("9008004"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.PAYMENT);
        transfer.setSourceMessage("TEXT");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("abc");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/icabanken/wiremock/resources/icaBankenGiroPaymentWithExecutionDateEqualsNull.aap";

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "icabanken-bankid", wireMockFilePath)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithoutLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWithExecutionDateWhenFutureBusinessDate() throws Exception {
        // given
        IcaBankenExecutorUtils.setClock(fixedClock("2021-09-21T15:27:56Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("92732490005"));
        transfer.setDestination(new BankGiroIdentifier("9008004"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.PAYMENT);
        transfer.setSourceMessage("TEXT");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("abc");
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 9, 24).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/icabanken/wiremock/resources/icaBankenGiroPaymentWithExecutionDateWhenFutureBusinessDate.aap";

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "icabanken-bankid", wireMockFilePath)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithoutLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToUsersOwnAccountWithinSameBank() throws Exception {
        // given
        IcaBankenExecutorUtils.setClock(fixedClock("2021-09-21T15:34:25Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("92732490005"));
        transfer.setDestination(new SwedishIdentifier("92732680000"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(2));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSourceMessage("TEXT");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("abc");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/icabanken/wiremock/resources/icaBankenPaymentToUsersOwnAccountWithinSameBank.aap";

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "icabanken-bankid", wireMockFilePath)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithoutLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToAnotherPersonsAccountWithinSameBankWithExecutionDateEqualsNull()
            throws Exception {
        // given
        IcaBankenExecutorUtils.setClock(fixedClock("2021-09-21T15:43:00Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("92732490005"));
        AccountIdentifier identifier =
                AccountIdentifier.create(AccountIdentifierType.SE, "92732340000", "tinkTest");
        transfer.setDestination(identifier);
        transfer.setAmount(ExactCurrencyAmount.inSEK(2));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSourceMessage("");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("tinkTest");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/icabanken/wiremock/resources/icaBankenPaymentToAnotherPersonsAccountWithinSameBankWithExecutionDateEqualsNull.aap";

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "icabanken-bankid", wireMockFilePath)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithoutLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToAnotherBankWithExecutionDateEqualsNull() throws Exception {
        // given
        IcaBankenExecutorUtils.setClock(fixedClock("2021-09-21T15:45:39Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("92732490005"));
        AccountIdentifier identifier =
                AccountIdentifier.create(AccountIdentifierType.SE, "33820000000", "tinkTest");
        transfer.setDestination(identifier);
        transfer.setAmount(ExactCurrencyAmount.inSEK(2));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSourceMessage("");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("tinkTest");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/icabanken/wiremock/resources/icaBankenPaymentToAnotherBankWithExecutionDateEqualsNull.aap";

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "icabanken-bankid", wireMockFilePath)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithoutLogin(TransferCommand.class);

        // when then
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWhereUserCancelSigningOfPayment() throws Exception {
        // given
        IcaBankenExecutorUtils.setClock(fixedClock("2021-09-21T15:48:22Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("92732490005"));
        transfer.setDestination(new BankGiroIdentifier("9008004"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.PAYMENT);
        transfer.setSourceMessage("");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("abc");
        transfer.setRemittanceInformation(remittanceInformation);

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/icabanken/wiremock/resources/icaBankenGiroPaymentWhereUserCancelSigningOfPayment.aap";

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "icabanken-bankid", wireMockFilePath)
                        .withHttpDebugTrace()
                        .withTransfer(transfer)
                        .buildWithoutLogin(TransferCommand.class);

        // when then
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (TransferExecutionException e) {
            Assert.assertEquals(
                    "You cancelled the BankID process. Please try again.", e.getMessage());
        }
    }

    private Clock fixedClock(String moment) {
        return Clock.fixed(Instant.parse(moment), DEFAULT_ZONE_ID);
    }
}
