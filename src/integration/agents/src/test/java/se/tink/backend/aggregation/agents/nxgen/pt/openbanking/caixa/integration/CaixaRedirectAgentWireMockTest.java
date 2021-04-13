package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixa.integration;

import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.module.SibsWireMockTestModule;
import se.tink.backend.aggregation.agents.utils.fixtures.WireMockTestFixtures.Properties;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Payment;

public class CaixaRedirectAgentWireMockTest {
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/openbanking/caixa/integration/resources/";

    private static CaixaRedirectAgentWireMockTestFixtures defaultFixtures;

    @BeforeClass
    public static void initialize() {
        Properties properties =
                Properties.builder()
                        .resourcesPath(RESOURCE_PATH)
                        .providerName("pt-caixa-ob")
                        .configurationFileName("configuration.yml")
                        .marketCode(MarketCode.PT)
                        .currency("EUR")
                        .destinationIdentifier("PT50001800034278472802055")
                        .accountIdentifierType(AccountIdentifierType.IBAN)
                        .remittanceInfoValue("PT Demo")
                        .build();

        defaultFixtures = new CaixaRedirectAgentWireMockTestFixtures(properties);
    }

    @Test
    public void testSuccessfulPayment() throws Exception {
        // given
        final String filename = "caixa_pt_payment_successful_wiremock.aap";
        final Payment payment = defaultFixtures.createDomesticPayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                defaultFixtures
                        .getPreconfiguredAgentWireMockPaymentTestBuilder(filename, payment)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addCallbackData("tpcb", "{\"key\":\"value\"}")
                        .withAgentModule(new SibsWireMockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        // when
        agentWireMockPaymentTest.executePayment();
    }
}
