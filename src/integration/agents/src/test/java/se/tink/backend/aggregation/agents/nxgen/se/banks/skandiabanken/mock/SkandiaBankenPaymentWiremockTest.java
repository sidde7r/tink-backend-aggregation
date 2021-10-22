package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.mock;

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
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.SkandiaBankenPaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class SkandiaBankenPaymentWiremockTest {
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");

    private static final String RESOURCE_PACKAGE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/skandiabanken/mock/resources/";

    @Test
    public void testBankGiroPaymentWithFutureExecutionDate() throws Exception {
        SkandiaBankenPaymentExecutor.setClockForTesting(fixedClock("2021-10-20T08:21:00.000Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("91599999999"));
        transfer.setDestination(new BankGiroIdentifier("9999999"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.PAYMENT);
        transfer.setSourceMessage("Tink source");
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 10, 30).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Some value");

        transfer.setRemittanceInformation(remittanceInformation);

        final String wiremockFilePath =
                RESOURCE_PACKAGE + "skandiabanken-mock-pis-future-successful.aap";

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "skandiabanken-ssn-bankid", wiremockFilePath)
                        .addPersistentStorageData(
                                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, getToken())
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testBankGiroPaymentWithExecutionDateEqualsNull() throws Exception {
        SkandiaBankenPaymentExecutor.setClockForTesting(fixedClock("2021-10-22T10:20:00.000Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("91599999999"));
        transfer.setDestination(new BankGiroIdentifier("9999999"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.PAYMENT);
        transfer.setSourceMessage("Tink source");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Some value");

        transfer.setRemittanceInformation(remittanceInformation);

        final String wiremockFilePath =
                RESOURCE_PACKAGE + "skandiabanken-mock-pis-null-execution-date.aap";

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "skandiabanken-ssn-bankid", wiremockFilePath)
                        .addPersistentStorageData(
                                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, getToken())
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testCancelledBankGiroPayment() throws Exception {
        SkandiaBankenPaymentExecutor.setClockForTesting(fixedClock("2021-10-20T08:21:00.000Z"));

        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("91599999999"));
        transfer.setDestination(new BankGiroIdentifier("9999999"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1));
        transfer.setType(TransferType.PAYMENT);
        transfer.setSourceMessage("Tink source");
        transfer.setDueDate(
                Date.from(LocalDate.of(2021, 10, 30).atStartOfDay(DEFAULT_ZONE_ID).toInstant()));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Some value");

        transfer.setRemittanceInformation(remittanceInformation);

        final String wiremockFilePath =
                RESOURCE_PACKAGE + "skandiabanken-mock-pis-payment-cancelled.aap";

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "skandiabanken-ssn-bankid", wiremockFilePath)
                        .addPersistentStorageData(
                                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, getToken())
                        .withTransfer(transfer)
                        .buildWithLogin(TransferCommand.class);

        try {
            agentWireMockPaymentTest.executePayment();
        } catch (TransferExecutionException e) {
            Assert.assertEquals("User cancelled signing of payment.", e.getMessage());
        }
    }

    private String getToken() {
        return SerializationUtils.serializeToString(
                OAuth2Token.create("Bearer", "accessToken", "refreshToken", 900));
    }

    private Clock fixedClock(String moment) {
        return Clock.fixed(Instant.parse(moment), DEFAULT_ZONE_ID);
    }
}
