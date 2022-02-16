package se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank.wiremock;

import static se.tink.libraries.enums.MarketCode.NO;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.danskebank.wiremock.module.DanskeBankNoWireMockTestModule;
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

public class DanskeBankNoPisWireMockTest {

    @Test
    public void testStandardPaymentWithUnstructuredInfo() throws Exception {

        // given
        final String configurationPath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/danskebank/wiremock/resources/configuration.yml";
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/danskebank/wiremock/resources/domestic_payment.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(NO, "no-danskebank-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(createDomesticPayment())
                        .withHttpDebugTrace()
                        .addCallbackData("code", "dummyCode")
                        .withAgentModule(new DanskeBankNoWireMockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("TinkTest-2022-01-24-09-22");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("NO9411111234567");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Tink Test");
        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("NO1622221234567");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inNOK(1.00);
        String currency = "NOK";

        return new Payment.Builder()
                .withUniqueId("f3fd90e27fb911eca8a30242ac120002")
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER)
                .build();
    }
}
