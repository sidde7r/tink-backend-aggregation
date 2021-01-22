package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.mock;

import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SEBWireMockPaymentTest {

    private static final String CONFIGURATION_PATH =
            "data/agents/openbanking/seb/configuration.yml";

    @Test
    public void testPayment() throws Exception {

        // given
        final String wireMockFilePath = "data/agents/openbanking/seb/wiremock-seb-ob-pis-pg.aap";
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
    public void testPaymentIncorrectlyCancelled() throws Exception {

        // given
        final String wireMockFilePath = "data/agents/openbanking/seb/wiremock-seb-ob-cancelled.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "se-seb-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createMockedDomesticBGPayment())
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .buildWithLogin(PaymentCommand.class);

        try {
            agentWireMockPaymentTest.executePayment();
            Assert.fail();
        } catch (PaymentAuthorizationException e) {
            Assert.assertEquals(
                    "BankID signing of payment was cancelled by the user.", e.getMessage());
        }
    }

    // TODO: Update the aap file for this test from a recent successful payment
    @Ignore("This likely fails because of an outdated aap file")
    @Test
    public void testTransfer() throws Exception {

        // given
        final String wireMockFilePath =
                "data/agents/openbanking/seb/wireMock-seb-ob-pis-transfer.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.SE, "se-seb-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createTransfer())
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), "197710120000")
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
        Assert.assertTrue(true);
    }

    private Payment createMockedDomesticPGPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("1047514784933");

        return new Payment.Builder()
                .withCreditor(new Creditor(AccountIdentifier.create(Type.SE_PG, "5768353"), "Tink"))
                .withDebtor(
                        new Debtor(AccountIdentifier.create(Type.IBAN, "SE4550000000058398257466")))
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
                .withCreditor(new Creditor(AccountIdentifier.create(Type.SE_BG, "5768353"), "Tink"))
                .withDebtor(
                        new Debtor(AccountIdentifier.create(Type.IBAN, "SE4550000000058398257466")))
                .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(585.57))
                .withCurrency("SEK")
                .withRemittanceInformation(remittanceInformation)
                .withExecutionDate(LocalDate.parse("2021-01-21"))
                .build();
    }

    private Payment createTransfer() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Tinktest");

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(AccountIdentifier.create(Type.SE, "56242222222"), "Tink"))
                .withDebtor(
                        new Debtor(AccountIdentifier.create(Type.IBAN, "SE4550000000058398257466")))
                .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(328.0))
                .withCurrency("SEK")
                .withRemittanceInformation(remittanceInformation)
                .withExecutionDate(LocalDate.parse("2020-06-22"))
                .build();
    }
}
