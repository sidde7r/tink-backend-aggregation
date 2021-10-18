package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.wiremock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.payment.DuplicatePaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils.SebDateUtil;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SEBWireMockPaymentTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/seb/wiremock/resources/configuration.yml";

    @Test
    public void testPayment() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/seb/wiremock/resources/wiremock-seb-ob-pis-pg.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "se-seb-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createMockedDomesticPGPayment())
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .buildWithLogin(PaymentCommand.class);
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testInternalTransfer() throws Exception {

        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/seb/wiremock/resources/wireMock-seb-ob-pis-transfer.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "se-seb-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createTransfer())
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWithNullExecutionDate() throws Exception {
        SebDateUtil.setClock(fixedClock("2021-09-29T01:51:58.622Z"));
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/seb/wiremock/resources/wireMock-seb-ob-giro-null-date.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "se-seb-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .withPayment(createMockedPayment())
                        .buildWithLogin(PaymentCommand.class);
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWithFutureExecutionDate() throws Exception {
        SebDateUtil.setClock(fixedClock("2021-09-29T01:51:58.622Z"));
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/seb/wiremock/resources/wireMock-seb-ob-giro-future.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "se-seb-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .withPayment(createMockedFuturePayment())
                        .buildWithLogin(PaymentCommand.class);
        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToAnotherBankWithNullExecutionDate() throws Exception {
        SebDateUtil.setClock(fixedClock("2021-09-28T01:51:58.622Z"));
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/seb/wiremock/resources/wiremock-seb-ob-pis-external-null-date.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "se-seb-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createTransferToAnotherBank())
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testCancelDueToDuplication() throws Exception {
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/seb/wiremock/resources/wireMock-seb-ob-pis-cancel-duplication.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "se-seb-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createMockedDomesticBGPayment())
                        .buildWithLogin(PaymentCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof DuplicatePaymentException);
            Assert.assertEquals(
                    "The payment could not be made because an identical payment is already registered",
                    e.getMessage());
        }
    }

    @Test
    public void testPaymentWithTooLongReference() throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/seb/wiremock/resources/wireMock-seb-ob-too-long-info.aap";
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "se-seb-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .withPayment(getPaymentWithTooLongReference())
                        .buildWithLogin(PaymentCommand.class);

        try {
            agentWireMockPaymentTest.executePayment();
            Assert.fail();
        } catch (ReferenceValidationException e) {
            Assert.assertEquals(
                    "Supplied payment reference is too long, max is 12 characters.",
                    e.getMessage());
        }
    }

    private Payment getPaymentWithTooLongReference() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("1234551234441");
        Payment payment =
                new Payment.Builder()
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.SE_BG, "9008004"),
                                        "Tink"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.IBAN,
                                                "SE4550000000058398257466")))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1.0))
                        .withCurrency("SEK")
                        .withExecutionDate(LocalDate.parse("2021-10-26"))
                        .withRemittanceInformation(remittanceInformation)
                        .build();
        return payment;
    }

    private Payment createMockedPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("123455123");

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(AccountIdentifierType.SE_BG, "9008004"),
                                "Tink"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, "SE4550000000058398257466")))
                .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1.0))
                .withCurrency("SEK")
                .withRemittanceInformation(remittanceInformation)
                .build();
    }

    private Payment createMockedFuturePayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("123455123");

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(AccountIdentifierType.SE_BG, "9008004"),
                                "Tink"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, "SE4550000000058398257466")))
                .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1.0))
                .withCurrency("SEK")
                .withExecutionDate(LocalDate.parse("2021-10-26"))
                .withRemittanceInformation(remittanceInformation)
                .build();
    }

    private Payment createMockedDomesticPGPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("1047514784933");

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(AccountIdentifierType.SE_PG, "5768353"),
                                "Tink"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, "SE4550000000058398257466")))
                .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(585.57))
                .withCurrency("SEK")
                .withRemittanceInformation(remittanceInformation)
                .withExecutionDate(LocalDate.parse("2021-01-21"))
                .build();
    }

    private Payment createMockedDomesticBGPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("1047514784933");

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(AccountIdentifierType.SE_BG, "5768353"),
                                "Tink"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, "SE4550000000058398257466")))
                .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(585.57))
                .withCurrency("SEK")
                .withRemittanceInformation(remittanceInformation)
                .withExecutionDate(LocalDate.parse("2021-01-21"))
                .build();
    }

    private Payment createTransfer() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("SBAB-BANK");

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(AccountIdentifierType.SE, "56242222222"),
                                "Joe Doe"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, "SE4550000000058398257466")))
                .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1000.0))
                .withCurrency("SEK")
                .withRemittanceInformation(remittanceInformation)
                .withExecutionDate(LocalDate.parse("2021-03-02"))
                .build();
    }

    private Payment createTransferToAnotherBank() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("123455123");

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(AccountIdentifierType.SE, "52733400843"),
                                "Tink"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, "SE4550000000058398257466")))
                .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1.0))
                .withCurrency("SEK")
                .withRemittanceInformation(remittanceInformation)
                .build();
    }

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");

    private Clock fixedClock(String moment) {
        return Clock.fixed(Instant.parse(moment), DEFAULT_ZONE_ID);
    }
}
