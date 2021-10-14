package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.wiremock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarDateUtil;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class LansforsakringarYellowPaymentWiremockTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/configuration.yml";

    @Test
    public void testGiroPaymentWithExecutionDateEqualsNull() throws Exception {
        reflectTheClock("2021-10-01T15:20:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/lfObGiroPaymentExecutionEqualsNull.aap";

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("54356060027588277");

        Payment payment =
                new Payment.Builder()
                        .withCreditor(new Creditor(new BankGiroIdentifier("51225860")))
                        .withDebtor(new Debtor(new SwedishIdentifier("90251234567")))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1.11d))
                        .withCurrency("SEK")
                        .withRemittanceInformation(remittanceInformation)
                        .withUniqueId("")
                        .build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-lansforsakringar-ob", wireMockFilePath)
                        .withPayment(payment)
                        .withHttpDebugTrace()
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "code")
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWithExecutionDateWhenFutureBusinessDate() throws Exception {
        reflectTheClock("2021-10-01T15:36:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/lfObGiroPaymentWithExecutionDateWhenFutureBusinessDate.aap";

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("54356060027588277");
        Payment payment =
                new Payment.Builder()
                        .withCreditor(new Creditor(new BankGiroIdentifier("51225860")))
                        .withDebtor(new Debtor(new SwedishIdentifier("90251234567")))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1.11d))
                        .withExecutionDate(LocalDate.parse("2022-09-30"))
                        .withCurrency("SEK")
                        .withRemittanceInformation(remittanceInformation)
                        .withUniqueId("")
                        .build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-lansforsakringar-ob", wireMockFilePath)
                        .withPayment(payment)
                        .withHttpDebugTrace()
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "code")
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testPaymentToAnotherBankWithExecutionDateEqualsNull() throws Exception {
        reflectTheClock("2021-01-20T15:42:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/lfObPaymentToAnotherBankWithExecutionDateEqualsNull.aap";

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("A2A123");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Payment payment =
                new Payment.Builder()
                        .withCreditor(new Creditor(new SwedishIdentifier("6410123456")))
                        .withDebtor(new Debtor(new SwedishIdentifier("90251234567")))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1.11d))
                        .withCurrency("SEK")
                        .withRemittanceInformation(remittanceInformation)
                        .withUniqueId("")
                        .build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-lansforsakringar-ob", wireMockFilePath)
                        .withPayment(payment)
                        .withHttpDebugTrace()
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "code")
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    @Test
    public void testGiroPaymentWhereUserCancelSigningOfPayment() throws Exception {
        reflectTheClock("2021-09-28T07:59:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/lfObGiroPaymentWhereUserCancelSigningOfPayment.aap";

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("A2A123");

        Payment payment =
                new Payment.Builder()
                        .withCreditor(new Creditor(new SwedishIdentifier("6410123456")))
                        .withDebtor(new Debtor(new SwedishIdentifier("90251234567")))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1.11d))
                        .withCurrency("SEK")
                        .withRemittanceInformation(remittanceInformation)
                        .withUniqueId("")
                        .build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-lansforsakringar-ob", wireMockFilePath)
                        .withPayment(payment)
                        .withHttpDebugTrace()
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "code")
                        .buildWithLogin(PaymentCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (PaymentException e) {
            Assert.assertEquals("The payment was cancelled by the user.", e.getMessage());
            return;
        }
        Assert.fail();
    }

    @Test
    public void testGiroPaymentWithReferenceLengthXplusOne() throws Exception {
        reflectTheClock("2021-10-01T15:44:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/lfObGiroPaymentWithReferenceLengthXplusOne.aap";

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("54356060027588277543560600275");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Payment payment =
                new Payment.Builder()
                        .withCreditor(new Creditor(new SwedishIdentifier("6410123456")))
                        .withDebtor(new Debtor(new SwedishIdentifier("90251234567")))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1.11d))
                        .withExecutionDate(LocalDate.parse("2021-01-21"))
                        .withCurrency("SEK")
                        .withRemittanceInformation(remittanceInformation)
                        .withUniqueId("")
                        .build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-lansforsakringar-ob", wireMockFilePath)
                        .withPayment(payment)
                        .withHttpDebugTrace()
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "code")
                        .buildWithLogin(PaymentCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (PaymentException e) {
            Assert.assertEquals("The message given is not valid", e.getMessage());
            return;
        }
        Assert.fail();
    }

    @Test
    public void testA2APaymentWithReferenceLengthXplusOne() throws Exception {
        reflectTheClock("2021-10-01T15:46:30Z");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/lansforsakringar/wiremock/resources/lfObA2APaymentWithReferenceLengthXplusOne.aap";

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("A2A12345678901234567890");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Payment payment =
                new Payment.Builder()
                        .withCreditor(new Creditor(new SwedishIdentifier("6410123456")))
                        .withDebtor(new Debtor(new SwedishIdentifier("90251234567")))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1.11d))
                        .withCurrency("SEK")
                        .withRemittanceInformation(remittanceInformation)
                        .withUniqueId("")
                        .build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE, "se-lansforsakringar-ob", wireMockFilePath)
                        .withPayment(payment)
                        .withHttpDebugTrace()
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "code")
                        .buildWithLogin(PaymentCommand.class);
        try {
            agentWireMockPaymentTest.executePayment();
        } catch (PaymentException e) {
            Assert.assertTrue(e.getMessage().startsWith("The message given is not valid"));
            return;
        }
        Assert.fail();
    }

    private void reflectTheClock(String datetime)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Clock clock = Clock.fixed(Instant.parse(datetime), ZoneId.of("CET"));
        Method m = LansforsakringarDateUtil.class.getDeclaredMethod("setClock", Clock.class);
        m.setAccessible(true);
        m.invoke(null, clock);
    }
}
