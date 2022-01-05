package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.mock;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.Month;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DnbAgentPaymentWiremockTest {
    private static final String RESOURCES_BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/mock/resources/";

    @Test
    public void testStandardPayment() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(getResourcePath("configuration.yml"));

        final AgentWireMockPaymentTest agentWireMockRefreshTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.NO,
                                "no-dnb-ob",
                                getResourcePath("dnb_payment_domestic_standard.aap"))
                        .withConfigurationFile(configuration)
                        .withPayment(createStandardPayment())
                        .withHttpDebugTrace()
                        .addCallbackData("code", "dummyCode")
                        .buildWithoutLogin(PaymentCommand.class);

        // then
        assertThatCode(agentWireMockRefreshTest::executePayment).doesNotThrowAnyException();
    }

    @Test
    public void testInstantPayment() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(getResourcePath("configuration.yml"));

        final AgentWireMockPaymentTest agentWireMockRefreshTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.NO,
                                "no-dnb-ob",
                                getResourcePath("dnb_payment_domestic_instant.aap"))
                        .withConfigurationFile(configuration)
                        .withPayment(createInstantPayment())
                        .withHttpDebugTrace()
                        .addCallbackData("code", "dummyCode")
                        .buildWithoutLogin(PaymentCommand.class);

        // then
        assertThatCode(agentWireMockRefreshTest::executePayment).doesNotThrowAnyException();
    }

    @Test
    public void testRecurringPayment() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(getResourcePath("configuration.yml"));

        final AgentWireMockPaymentTest agentWireMockRefreshTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.NO,
                                "no-dnb-ob",
                                getResourcePath("dnb_payment_domestic_recurring.aap"))
                        .withConfigurationFile(configuration)
                        .withPayment(createRecurringPayment())
                        .withHttpDebugTrace()
                        .addCallbackData("code", "dummyCode")
                        .buildWithoutLogin(PaymentCommand.class);

        // then
        assertThatCode(agentWireMockRefreshTest::executePayment).doesNotThrowAnyException();
    }

    private Payment createStandardPayment() {
        return createPayment(
                PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER, PaymentServiceType.SINGLE);
    }

    private Payment createInstantPayment() {
        return createPayment(
                PaymentScheme.INSTANT_NORWEGIAN_DOMESTIC_CREDIT_TRANSFER_STRAKS,
                PaymentServiceType.SINGLE);
    }

    private Payment createRecurringPayment() {
        Payment payment =
                createPayment(
                        PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER,
                        PaymentServiceType.PERIODIC);
        return payment;
    }

    private Payment createPayment(
            PaymentScheme paymentScheme, PaymentServiceType paymentServiceType) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("610550873500157");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("NO1060310977686");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "John Smith");

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("NO8112255428177");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inNOK(0.01);
        String currency = "NOK";

        return new Payment.Builder()
                .withUniqueId("232539fc-dfc3-47ed-8c70-205a928f05c8")
                .withPaymentScheme(paymentScheme)
                .withPaymentServiceType(paymentServiceType)
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withExecutionDate(LocalDate.of(2030, Month.JANUARY, 15))
                .withFrequency(Frequency.MONTHLY)
                .withStartDate(LocalDate.of(2030, Month.JANUARY, 15))
                .withEndDate(LocalDate.of(2030, Month.JUNE, 15))
                .build();
    }

    private String getResourcePath(String fileName) {
        return RESOURCES_BASE_PATH + fileName;
    }
}
