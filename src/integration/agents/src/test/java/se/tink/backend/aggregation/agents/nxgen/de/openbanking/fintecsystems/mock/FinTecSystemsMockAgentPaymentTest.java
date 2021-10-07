package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems.mock;

import static org.assertj.core.api.Assertions.assertThatCode;

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
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class FinTecSystemsMockAgentPaymentTest {

    private static final String CONFIGURATION_FILE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fintecsystems/mock/resources/configuration.yml";

    @Test
    public void testSepaPaymentInitiation() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fintecsystems/mock/resources/pis.aap";

        Payment payment = createRealDomesticPayment().build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-test-fintecsystems", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(payment)
                        .buildWithoutLogin(PaymentCommand.class);
        // when // then
        assertThatCode(agentWireMockPaymentTest::executePayment).doesNotThrowAnyException();
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("ReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("DE04888888880087654321");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation);
    }
}
