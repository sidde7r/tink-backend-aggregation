package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.mock;

import static se.tink.libraries.enums.MarketCode.NO;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.mock.module.NordeaWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class NordeaNoPaymentWireMockTest {

    @Test
    public void testStandardPayment() throws Exception {

        // given
        final String configurationPath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/nordea/mock/resources/configuration.yml";
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/nordea/mock/resources/paymentStandardWireMock.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(NO, "no-nordea-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(createStandardPayment())
                        .withHttpDebugTrace()
                        .addCallbackData("code", "dummyCode")
                        .withAgentModule(new NordeaWireMockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createStandardPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Tink-2022-02-04--13-05-49");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        // Ibans are randomly generated. It is needed to use properly formulated for sake of
        // checking verifiers in the code.
        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("NO5657553677653");
        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("NO1284766456533");

        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inNOK(5);
        String currency = "NOK";

        return new Payment.Builder()
                .withUniqueId("232539fc-dfc3-47ed-8c70-205a928f05c8")
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withExecutionDate(LocalDate.of(2022, 2, 4))
                .withRemittanceInformation(remittanceInformation)
                .build();
    }
}
