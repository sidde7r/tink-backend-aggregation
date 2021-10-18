package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.mock.payment;

import org.junit.BeforeClass;
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
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SpardaPaymentWiremockTest {
    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/sparda/mock/payment/resources/";

    private static AgentsServiceConfiguration configuration;

    @BeforeClass
    public static void setup() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(BASE_PATH + "configuration.yml");
    }

    @Test
    public void testSepaPaymentInitiation() throws Exception {
        final String wireMockFilePath = BASE_PATH + "sepa.aap";

        Payment payment =
                createRealDomesticPayment()
                        .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                        .build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-sparda-nurnberg-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCredentialField("username", "dummy_psu_id")
                        .addCredentialField("password", "dummy_password")
                        .addCallbackData("selectAuthMethodField", "6")
                        .withPayment(payment)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .buildWithoutLogin(PaymentCommand.class);
        agentWireMockPaymentTest.executePayment();
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("SepaReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("DE95760400610770740900");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "dummy owner");

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("DE83760905000004930045");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation);
    }
}
