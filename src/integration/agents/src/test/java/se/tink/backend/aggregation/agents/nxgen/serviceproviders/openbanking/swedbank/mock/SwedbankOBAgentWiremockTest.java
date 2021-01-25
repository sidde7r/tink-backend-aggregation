package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.mock;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SwedbankOBAgentWiremockTest {
    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/swedbank/mock/resources/configuration.yml";
    private static final String WIRE_MOCK_PAYMENT_WITH_NEW_RECIPIENT =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/swedbank/mock/resources/wireMock-swedbank-ob-pis.aap";

    @Test
    public void testPaymentWithNewRecipient() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE,
                                "se-swedbank-ob",
                                WIRE_MOCK_PAYMENT_WITH_NEW_RECIPIENT)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createMockedDomesticPayment())
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment() {
        Debtor debtor =
                new Debtor(AccountIdentifier.create(AccountIdentifier.Type.SE, "10987654321"));
        Creditor creditor =
                new Creditor(AccountIdentifier.create(Type.SE, "12345678901"), "TinkTest");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("tinkTest");

        return new Payment.Builder()
                .withDebtor(debtor)
                .withCreditor(creditor)
                .withExecutionDate(LocalDate.of(2021, 1, 26))
                .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1))
                .withRemittanceInformation(remittanceInformation)
                .build();
    }
}
