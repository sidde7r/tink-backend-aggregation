package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditmutuel.integration;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditmutuel.integration.module.CreditMutuelWireMockTestModule;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

public class CreditMutuelWireMockTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/creditmutuel/integration/resources/configuration.yml";

    @Test
    public void testSepaInstantPayment() throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/creditmutuel/integration/resources/creditmutuel_mock_instant_payment_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.FR, "fr-creditmutuel-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addCallbackData("psuAF", "DUMMY_PSU_AUTH_CODE")
                        .withHttpDebugTrace()
                        .withPayment(
                                createRealDomesticPayment(
                                        PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER, null))
                        .withAgentModule(new CreditMutuelWireMockTestModule())
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testSepaPayment() throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/creditmutuel/integration/resources/creditmutuel_mock_payment_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.FR, "fr-creditmutuel-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addCallbackData("psuAF", "DUMMY_PSU_AUTH_CODE")
                        .withHttpDebugTrace()
                        .withPayment(
                                createRealDomesticPayment(
                                        PaymentScheme.SEPA_CREDIT_TRANSFER,
                                        LocalDate.of(2021, 4, 20)))
                        .withAgentModule(new CreditMutuelWireMockTestModule())
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void test() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/creditmutuel/integration/resources/creditmutuel_mock_log.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/creditmutuel/integration/resources/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.FR, "fr-creditmutuel-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .withAgentModule(new CreditMutuelWireMockTestModule())
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    private Payment createRealDomesticPayment(
            PaymentScheme paymentScheme, LocalDate executionDate) {
        AccountIdentifier creditorAccountIdentifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, "FR7630004033770000100375924");

        AccountIdentifier debtorAccountIdentifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, "FR7610278022230002054000114");

        return new Payment.Builder()
                .withCreditor(new Creditor(creditorAccountIdentifier))
                .withDebtor(new Debtor(debtorAccountIdentifier))
                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1.0))
                .withCurrency("EUR")
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "Message"))
                .withExecutionDate(executionDate)
                .withPaymentScheme(paymentScheme)
                .build();
    }
}
