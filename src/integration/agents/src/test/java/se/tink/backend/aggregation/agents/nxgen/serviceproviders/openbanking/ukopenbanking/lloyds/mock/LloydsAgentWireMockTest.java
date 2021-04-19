package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.lloyds.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;

public class LloydsAgentWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/lloyds/mock/resources/";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";
    private static final String SUCCESSFUL_PAYMENT_MOCK_FILE =
            RESOURCES_PATH + "uk-lloyds-successful-payment.aap";
    private static final String CANCELLED_PAYMENT_MOCK_FILE =
            RESOURCES_PATH + "uk-lloyds-cancelled-payment.aap";

    private static final String PROVIDER_NAME = "uk-lloyds-oauth2";
    private static final String STATE = "00000000-0000-4000-0000-000000000000";

    @Test
    public void testManualRefresh() throws Exception {

        // given
        final String wireMockFilePath = RESOURCES_PATH + "uk-lloyds-manual.aap";
        final String contractFilePath = RESOURCES_PATH + "uk-lloyds-manual.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName("uk-lloyds-oauth2")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(
                                RefreshableItem.CHECKING_ACCOUNTS,
                                RefreshableItem.CHECKING_TRANSACTIONS)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
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
    public void testSuccessfulPayment() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithAuthCodeCallbackData(configuration);

        // then
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testCancelledPayment() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithErrorCallbackData(configuration);

        // when
        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationCancelledByUserException.class)
                .hasNoCause()
                .hasMessage("Authorisation of payment was cancelled. Please try again.");
    }

    private AgentWireMockPaymentTest createAgentWireMockPaymentTestWithAuthCodeCallbackData(
            AgentsServiceConfiguration configuration) {
        return AgentWireMockPaymentTest.builder(
                        MarketCode.UK, PROVIDER_NAME, SUCCESSFUL_PAYMENT_MOCK_FILE)
                .withConfigurationFile(configuration)
                .withPayment(createMockedDomesticPayment())
                .addCallbackData("code", "DUMMY_AUTH_CODE")
                .buildWithoutLogin(PaymentCommand.class);
    }

    private AgentWireMockPaymentTest createAgentWireMockPaymentTestWithErrorCallbackData(
            AgentsServiceConfiguration configuration) {
        return AgentWireMockPaymentTest.builder(
                        MarketCode.UK, PROVIDER_NAME, CANCELLED_PAYMENT_MOCK_FILE)
                .withConfigurationFile(configuration)
                .addCallbackData("state", STATE)
                .addCallbackData("error", "access_denied")
                .withPayment(createMockedDomesticPayment())
                .buildWithoutLogin(PaymentCommand.class);
    }

    private Payment createMockedDomesticPayment() {
        String currency = "GBP";
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", currency);
        LocalDate executionDate = LocalDate.now();
        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.SORT_CODE, "12345678912345"),
                                "Creditor Name"))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "DUMMY_REMITTANCE"))
                .withUniqueId("0b57b778df264b21bb3cfab3a20841d2")
                .build();
    }
}
