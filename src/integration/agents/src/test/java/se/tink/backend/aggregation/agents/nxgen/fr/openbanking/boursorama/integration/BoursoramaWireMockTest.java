package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.integration;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.integration.module.BoursoramaWireMockTestModule;
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

@RunWith(JUnitParamsRunner.class)
public class BoursoramaWireMockTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/boursorama/integration/resources/configuration.yml";
    private static final LocalDate TODAY = LocalDate.of(1992, 4, 10);
    private static final LocalDate TWO_DAYS_AFTER_TODAY = TODAY.plusDays(2);

    @Test
    public void testSepaPayment() throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/boursorama/integration/resources/boursorama_sepa_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.FR, "fr-boursorama-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .withHttpDebugTrace()
                        .withPayment(
                                createRealDomesticPayment(
                                        PaymentScheme.SEPA_CREDIT_TRANSFER,
                                        TWO_DAYS_AFTER_TODAY,
                                        1.23))
                        .withAgentModule(new BoursoramaWireMockTestModule())
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testSepaInstantPayment() throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/boursorama/integration/resources/boursorama_sepa_instant_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.FR, "fr-boursorama-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .withHttpDebugTrace()
                        .withPayment(
                                createRealDomesticPayment(
                                        PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER, TODAY, 1.0))
                        .withAgentModule(new BoursoramaWireMockTestModule())
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    @Parameters({"1992-04-08", "1992-04-12"})
    public void shouldThrowOnSepaInstantPaymentIfDateNotToday(String executionDate)
            throws Exception {

        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/boursorama/integration/resources/boursorama_sepa_instant_invalid_date_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.FR, "fr-boursorama-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .withHttpDebugTrace()
                        .withPayment(
                                createRealDomesticPayment(
                                        PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER,
                                        LocalDate.parse(executionDate),
                                        1.0))
                        .withAgentModule(new BoursoramaWireMockTestModule())
                        .buildWithLogin(PaymentCommand.class);

        Throwable exception = catchThrowable(agentWireMockPaymentTest::executePayment);

        assertThat(exception).isExactlyInstanceOf(PaymentValidationException.class);
    }

    @Test
    public void test() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/boursorama/integration/resources/boursorama_mock_log.aap";
        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/boursorama/integration/resources/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(
                                MarketCode.FR, "fr-boursorama-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .withAgentModule(new BoursoramaWireMockTestModule())
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
            PaymentScheme paymentScheme, LocalDate executionDate, double amount) {
        AccountIdentifier creditorAccountIdentifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, "FR1420041010050500013M02606");

        AccountIdentifier debtorAccountIdentifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, "FR1261401750597365134612940");

        return new Payment.Builder()
                .withCreditor(new Creditor(creditorAccountIdentifier, "Creditor Name"))
                .withDebtor(new Debtor(debtorAccountIdentifier))
                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(amount))
                .withCurrency("EUR")
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "Message"))
                .withExecutionDate(executionDate)
                .withPaymentScheme(paymentScheme)
                .build();
    }
}
