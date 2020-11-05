package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.AUTH_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.PROVIDER_NAME;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.createDomesticPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.createFarFutureDomesticPayment;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentGBCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Payment;

public class SantanderAgentWiremockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/santander/integration/resources/";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";

    @Test
    public void testPaymentSuccessfulPayment() throws Exception {
        // given
        final String wireMockFilePath =
                RESOURCES_PATH + "santander_payment_successful_case_mock_log.aap";
        final Payment payment = createDomesticPayment();
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTest(wireMockFilePath, payment);

        // when
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testPaymentFailedCase() throws Exception {
        // given
        final String wireMockFilePath =
                RESOURCES_PATH + "santander_payment_failed_case_mock_log.aap";
        final Payment payment = createFarFutureDomesticPayment();
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTest(wireMockFilePath, payment);

        // when
        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(HttpResponseException.class)
                .hasNoCause()
                .hasMessage(
                        "Response statusCode: 400 with body: {\"Code\":\"400\",\"Message\":\"There is something wrong with the request parameters provided\",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Field.InvalidDate\",\"Message\":\"The date field data.initiation.requestedExecutionDateTime is invalid\"}]}");
    }

    private static AgentWireMockPaymentTest createAgentWireMockPaymentTest(
            String wireMockFilePath, Payment payment) throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, wireMockFilePath)
                .withConfigurationFile(configuration)
                .addCallbackData("code", AUTH_CODE)
                .addPayment(payment)
                .buildWithoutLogin(PaymentGBCommand.class);
    }
}
