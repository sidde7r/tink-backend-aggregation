package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.integration;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.integration.module.BpceGroupWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class CaisseEpargneIleDeFranceWireMockTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/bpcegroup/integration/resources/configuration.yml";

    @Test
    public void testPayment() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/bpcegroup/integration/resources/bpce_payment_mock_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.FR, "fr-caisseepargneiledefrance-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addPayment(createMockedDomesticPayment())
                        .withAgentModule(new BpceGroupWireMockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        // when / then (execution and assertion currently done in the same step)
        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment() {
        String currency = "EUR";
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", currency);

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(Type.SORT_CODE, "04000469430924"), "Tink"))
                .withDebtor(new Debtor(AccountIdentifier.create(Type.SORT_CODE, "2314701111111")))
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withReference(new Reference("TRANSFER", "test"))
                .withUniqueId("paymentId")
                .build();
    }
}
