package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.wiremock;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.wiremock.module.SparebankWiremockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SparebankPisWiremockTest {

    @Test
    public void testDomesticCreditTransfer() throws Exception {
        // given
        final String configurationFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/wiremock/resources/configuration.yml";
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/wiremock/resources/pis-credit-transfer-sparebank-ob.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationFilePath);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.NO, "no-sparebank1sr-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(createDomesticPayment())
                        .withHttpDebugTrace()
                        .addCallbackData("code", "dummyCode")
                        .withAgentModule(new SparebankWiremockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("PIS Wiremock Test");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new BbanIdentifier("88888888888");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor");
        AccountIdentifier debtorAccountIdentifier = new BbanIdentifier("33333333333");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inNOK(2.00);
        String currency = "NOK";
        String executionDate = "2022-01-19";

        return new Payment.Builder()
                .withUniqueId(
                        "enc!!Udi4TNWHndLbrPRFIIAsON1i24ezT2IL9VGjsZwKH_q9xBI-IQ2IV_SEvSDaQfF4")
                .withExecutionDate(LocalDate.parse(executionDate))
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER)
                .withPaymentServiceType(PaymentServiceType.SINGLE)
                .build();
    }
}
