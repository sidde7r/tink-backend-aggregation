package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.rbs.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentGBCommand;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;

public class RbsAgentWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/rbs/integration/resources/";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";

    private final String DESTINATION_IDENTIFIER = "04000469431111";

    @Test
    public void testSuccessfulPayment() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "payment_successful_case_mock_log.aap";
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithAuthCodeCallbackData(wireMockFilePath);

        // when
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testFailedPayment() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "payment_failed_case_mock_log.aap";
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithAuthCodeCallbackData(wireMockFilePath);

        // when
        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(HttpResponseException.class)
                .hasNoCause()
                .hasMessage(
                        "Response statusCode: 400 with body: {\"Code\":\"400 BadRequest\",\"Id\":\"38838199-e299-4c3a-ad16-d5058b6ced21\",\"Message\":\"Request error found.\",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Field.Invalid\",\"Message\":\"Creditor account failed to pass validation checks\"}]}");
    }

    private AgentWireMockPaymentTest createAgentWireMockPaymentTestWithAuthCodeCallbackData(
            String wireMockFilePath) throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        return AgentWireMockPaymentTest.builder(MarketCode.UK, "uk-rbs-oauth2", wireMockFilePath)
                .withConfigurationFile(configuration)
                .addCallbackData("code", "DUMMY_AUTH_CODE")
                .addPayment(createMockedDomesticPayment())
                .buildWithoutLogin(PaymentGBCommand.class);
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
