package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.integration;

import java.time.LocalDate;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.integration.module.FiduciaWireMockTestModule;
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

public class FiduciaAgentWireMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia/integration/resources/";

    private static String contractFilePath;

    private static AgentsServiceConfiguration configuration;

    @BeforeClass
    public static void setup() throws Exception {
        contractFilePath = BASE_PATH + "agent-contract.json";
        configuration = AgentsServiceConfigurationReader.read(BASE_PATH + "configuration.yml");
    }

    @Test
    public void testOneScaMethod() throws Exception {
        wiremockTest(BASE_PATH + "fiducia_mock_log.aap", "pushTan");
    }

    @Test
    public void testScaMethodSelection() throws Exception {
        wiremockTest(BASE_PATH + "fiducia_with_sca_method_selection_mock_log.aap", "chipTan");
    }

    private void wiremockTest(String wiremockFilePath, String chosenMethod) throws Exception {

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.DE, "de-vrbank-raiba-muc-sued-ob", wiremockFilePath)
                        .addCredentialField("psu-id", "dummy_psu_id")
                        .addCredentialField("password", "dummy_password")
                        .withConfigurationFile(configuration)
                        .addCallbackData(chosenMethod, "dummy_otp_code")
                        .addCallbackData("selectAuthMethodField", "2")
                        .withAgentModule(new FiduciaWireMockTestModule())
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    @Ignore
    // This test is ignored due to bug in the wiremock thing, xml bodies are not compared (at all?
    // correctly?)
    public void testSepaPayment() throws Exception {
        // given

        Payment payment = createSepaPayment().withExecutionDate(LocalDate.of(2021, 6, 8)).build();
        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE,
                                "de-vrbank-raiba-muc-sued-ob",
                                BASE_PATH + "fiducia_sepa_payment.aap")
                        .withConfigurationFile(configuration)
                        .addCredentialField("psu-id", "dummy_psu_id")
                        .addCredentialField("password", "dummy_password")
                        .withPayment(payment)
                        .withHttpDebugTrace()
                        .buildWithoutLogin(PaymentCommand.class);

        // when
        agentWireMockPaymentTest.executePayment();
    }

    private Payment.Builder createSepaPayment() {
        return createRealDomesticPayment().withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER);
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("SepaReferenceToCreditor 1234");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("DE1234");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("DE4322");
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
