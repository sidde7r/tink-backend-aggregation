package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.mock;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
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

public class UnicreditPaymentMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/unicredit/mock/resources/";
    private static AgentsServiceConfiguration configuration;

    @BeforeClass
    public static void setup() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(BASE_PATH + "configuration.yml");
    }

    @Test
    public void testSepaPayment() {
        // given
        final String wireMockFilePath = BASE_PATH + "unicredit_sepa_payment.aap";

        Payment payment =
                createRealDomesticPayment()
                        .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                        .build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.DE, "de-unicredit-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCredentialField("username", "test_username")
                        .addCredentialField("password", "test_password")
                        .addCallbackData("smsTan", "123456")
                        .withPayment(payment)
                        .buildWithoutLogin(PaymentCommand.class);

        // when / then
        assertThatCode(agentWireMockPaymentTest::executePayment).doesNotThrowAnyException();
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("ReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.REFERENCE);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("DE95760400610770740900");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("DE40300209005380419896");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withExecutionDate(LocalDate.parse("2022-02-01"))
                .withRemittanceInformation(remittanceInformation);
    }
}
