package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.wiremock;

import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class AbancaPaymentWiremockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/openbanking/abanca/wiremock/resources/";
    static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";

    @Test
    public void testSuccessTransfer() throws Exception {

        // given
        final String wireMockFilePath = RESOURCES_PATH + "payment_success.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.ES, "es-abanca-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "dummyCode")
                        .withPayment(
                                createMockedPayment(
                                        getCreditor(),
                                        RemittanceInformationUtils
                                                .generateUnstructuredRemittanceInformation(
                                                        "Remittance")))
                        .withHttpDebugTrace()
                        .buildWithLogin(PaymentCommand.class);

        // then
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testInsufficientFunds() throws Exception {

        // given
        final String wireMockFilePath = RESOURCES_PATH + "payment_insufficient.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.ES, "es-abanca-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "dummyCode")
                        .withPayment(
                                createMockedPayment(
                                        getCreditor(),
                                        RemittanceInformationUtils
                                                .generateUnstructuredRemittanceInformation(
                                                        "Remittance")))
                        .withHttpDebugTrace()
                        .buildWithLogin(PaymentCommand.class);

        // when
        Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        Assertions.assertThat(thrown).isExactlyInstanceOf(InsufficientFundsException.class);
    }

    private Payment createMockedPayment(
            Creditor creditor, RemittanceInformation remittanceInformation) {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", "EUR");
        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(getDebtor())
                .withExactCurrencyAmount(amount)
                .withExecutionDate(LocalDate.parse("2021-06-25"))
                .withCurrency(amount.getCurrencyCode())
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId("")
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                .build();
    }

    private Creditor getCreditor() {
        return new Creditor(
                AccountIdentifier.create(
                        AccountIdentifierType.IBAN, "ES1904877999386443695937", "Creditor name"),
                "Creditor name");
    }

    private Debtor getDebtor() {
        return new Debtor(
                AccountIdentifier.create(
                        AccountIdentifierType.IBAN, "ES7020955321675342774892", "Debtor name"));
    }
}
