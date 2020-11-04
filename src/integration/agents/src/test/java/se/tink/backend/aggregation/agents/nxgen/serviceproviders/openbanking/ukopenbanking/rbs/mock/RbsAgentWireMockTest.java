package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.rbs.mock;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentGBCommand;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;

public class RbsAgentWireMockTest {

    private final String DESTINATION_IDENTIFIER = "04000469431111";

    private static final String CONFIGURATION_PATH = "data/agents/uk/rbs/configuration.yml";

    @Test
    public void testPayment() throws Exception {
        // given
        final String wireMockFilePath = "data/agents/uk/rbs/payment_mock_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.UK, "uk-rbs-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addPayment(createMockedDomesticPayment())
                        .buildWithoutLogin(PaymentGBCommand.class);

        // when
        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", "GBP");
        LocalDate executionDate = LocalDate.now();
        String currency = "GBP";
        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(Type.SORT_CODE, DESTINATION_IDENTIFIER),
                                "Dummy name"))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "UK Demo"))
                .withUniqueId("4f0a6efca9c740e781419944143cdf13")
                .build();
    }
}
