package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.wiremock;

import static se.tink.libraries.enums.MarketCode.FI;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.wiremock.module.DanskeBankWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DanskeBankPisWireMockTest {

    @Test
    public void testStandardPayment() throws Exception {

        // given
        final String configurationPath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/danskebank/wiremock/resources/configuration.yml";
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/danskebank/wiremock/resources/domestic_payment.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(FI, "fi-danskebank-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(createDomesticPayment())
                        .withHttpDebugTrace()
                        .addCallbackData("code", "dummyCode")
                        .withAgentModule(new DanskeBankWireMockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Tink Test-2021-12-06--13-46-51");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        // ibans are mocked
        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("FI3126598515147343");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Tink Test");
        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("FI9189512712449566");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1.00);
        String currency = "EUR";

        return new Payment.Builder()
                .withUniqueId("258b967552ae47579f183b57514f1413")
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                .build();
    }
}
