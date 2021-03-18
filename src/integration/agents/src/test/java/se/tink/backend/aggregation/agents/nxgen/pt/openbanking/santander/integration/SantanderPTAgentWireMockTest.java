package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.santander.integration;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.utils.fixtures.WireMockTestFixtures.Properties;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Payment;

@Ignore("Broken test")
public class SantanderPTAgentWireMockTest {

    private static SantanderPTAgentWireMockTestFixtures defaultFixtures;

    @BeforeClass
    public static void initialize() {
        Properties properties =
                Properties.builder()
                        .resourcesPath(
                                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/openbanking/santander/integration/resources/")
                        .providerName("pt-santander-oauth2")
                        .configurationFileName("configuration.yml")
                        .marketCode(MarketCode.PT)
                        .currency("EUR")
                        .destinationIdentifier("PT50000201231234567890154")
                        .accountIdentifierType(AccountIdentifierType.IBAN)
                        .remittanceInfoValue("PT Demo")
                        .build();

        defaultFixtures = new SantanderPTAgentWireMockTestFixtures(properties);
    }

    @Test
    public void testPaymentSuccessfulPayment() throws Exception {
        // given
        final String filename = "santander_pt_payment_successful_case_mock_log.aap";
        final Payment payment = defaultFixtures.createDomesticPayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                defaultFixtures
                        .getPreconfiguredAgentWireMockPaymentTestBuilder(filename, payment)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addCallbackData("tpcb", "{\"key\":\"value\"}")
                        .buildWithLogin(PaymentCommand.class);

        // when
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testPaymentFailedCase() throws Exception {
        // given
        final String filename = "santander_pt_payment_failed_case_mock_log.aap";
        final Payment payment = defaultFixtures.createFarFutureDomesticPayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                defaultFixtures
                        .getPreconfiguredAgentWireMockPaymentTestBuilder(filename, payment)
                        .addCallbackData("error", "access_denied")
                        .buildWithLogin(PaymentCommand.class);

        // when
        final Throwable thrown =
                Assertions.catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        Assertions.assertThat(thrown)
                .isExactlyInstanceOf(AuthorizationException.class)
                .hasMessage("Authorization failed, consents status is not accepted.");
    }

    @Test
    public void testPaymentCancelledCase() throws Exception {
        // given
        final String filename = "santander_pt_payment_cancelled_case_mock_log.aap";
        final Payment payment = defaultFixtures.createDomesticPayment();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                defaultFixtures
                        .getPreconfiguredAgentWireMockPaymentTestBuilder(filename, payment)
                        .addCallbackData("error", "access_denied")
                        .buildWithLogin(PaymentCommand.class);

        // when
        final Throwable thrown =
                Assertions.catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        Assertions.assertThat(thrown).isExactlyInstanceOf(PaymentAuthorizationException.class);
    }
}
