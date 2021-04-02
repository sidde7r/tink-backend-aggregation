package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.mock;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SwedbankOBAgentWiremockTest {
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/swedbank/mock/resources";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "/configuration.yml";
    private static final String WIRE_MOCK_PAYMENT_WITH_NEW_RECIPIENT =
            RESOURCES_PATH + "/wireMock-swedbank-ob-pis.aap";
    private static final String WIRE_MOCK_CANCELLED_PAYMENT_WITH_NEW_RECIPIENT =
            RESOURCES_PATH + "/wiremock-swedbank-ob-pis-cancelled.aap";

    private AgentsServiceConfiguration configuration;

    @Before
    public void setup() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }

    @Test
    public void testPaymentWithNewRecipient() throws Exception {
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

    @Test(expected = PaymentAuthorizationException.class)
    public void testCancelledPaymentWithNewRecipient() throws Exception {
        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE,
                                "se-swedbank-ob",
                                WIRE_MOCK_CANCELLED_PAYMENT_WITH_NEW_RECIPIENT)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createMockedDomesticPayment())
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment() {
        Debtor debtor =
                new Debtor(AccountIdentifier.create(AccountIdentifierType.SE, "10987654321"));
        Creditor creditor =
                new Creditor(
                        AccountIdentifier.create(AccountIdentifierType.SE, "12345678901"),
                        "TinkTest");
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
