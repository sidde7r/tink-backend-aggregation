package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.AUTH_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.PROVIDER_NAME;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.createDomesticPayment;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentGBCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.enums.MarketCode;

public class SantanderAgentWiremockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/santander/integration/resources/";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";

    @Test
    public void testPayment() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "santander_payment_mock_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", AUTH_CODE)
                        .addPayment(createDomesticPayment())
                        .buildWithoutLogin(PaymentGBCommand.class);

        // when
        agentWireMockPaymentTest.executePayment();
    }
}
