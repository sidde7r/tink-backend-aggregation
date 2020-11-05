package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.AUTH_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.PROVIDER_NAME;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.STATE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.createDomesticPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.createFarFutureDomesticPayment;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
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
                createAgentWireMockPaymentTestWithAuthCodeCallbackData(wireMockFilePath, payment);

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
                createAgentWireMockPaymentTestWithAuthCodeCallbackData(wireMockFilePath, payment);

        // when
        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(HttpResponseException.class)
                .hasNoCause()
                .hasMessage(
                        "Response statusCode: 400 with body: {\"Code\":\"400\",\"Message\":\"There is something wrong with the request parameters provided\",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Field.InvalidDate\",\"Message\":\"The date field data.initiation.requestedExecutionDateTime is invalid\"}]}");
    }

    @Test
    public void testPaymentCancelledCase() throws Exception {
        // given
        final Payment payment = createDomesticPayment();
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithErrorCallbackData(payment);

        // when
        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationException.class)
                .hasNoCause()
                .hasMessage("Payment was not authorised. Please try again.");
    }

    private static AgentWireMockPaymentTest createAgentWireMockPaymentTestWithAuthCodeCallbackData(
            String wireMockFilePath, Payment payment) throws Exception {
        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, wireMockFilePath)
                .withConfigurationFile(readAgentConfiguration())
                .addCallbackData("code", AUTH_CODE)
                .addPayment(payment)
                .buildWithoutLogin(PaymentGBCommand.class);
    }

    private static AgentWireMockPaymentTest createAgentWireMockPaymentTestWithErrorCallbackData(
            Payment payment) throws Exception {
        final String wireMockFilePath =
                RESOURCES_PATH + "santander_payment_canceled_case_mock_log.aap";

        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, wireMockFilePath)
                .withConfigurationFile(readAgentConfiguration())
                .addCallbackData("state", STATE)
                .addCallbackData("error", "access_denied")
                .addPayment(payment)
                .buildWithoutLogin(PaymentGBCommand.class);
    }

    private static AgentsServiceConfiguration readAgentConfiguration() throws Exception {
        return AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }
}
