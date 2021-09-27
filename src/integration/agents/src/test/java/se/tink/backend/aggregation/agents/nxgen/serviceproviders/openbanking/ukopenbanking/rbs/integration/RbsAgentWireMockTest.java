package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.rbs.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentGBCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
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

public class RbsAgentWireMockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/rbs/integration/resources/";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";
    private static final String PROVIDER_NAME = "uk-rbs-oauth2";
    private static final Function<LocalDateTime, String> VALID_OAUTH2_TOKEN =
            localDateTime ->
                    String.format(
                            "{\"expires_in\" : 0, \"issuedAt\": %s, \"tokenType\":\"bearer\", \"refreshToken\":\"DUMMY_REFRESH_TOKEN\",  \"accessToken\":\"DUMMY_ACCESS_TOKEN\"}",
                            localDateTime.toEpochSecond(ZoneOffset.UTC));

    @Test
    public void shouldRunAutoAuthWithDataRefreshSuccessfully() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "auto-auth-fetch-data.aap";
        final String wireMockContractFilePath = RESOURCES_PATH + "auto-auth-fetch-data.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(
                                AgentsServiceConfigurationReader.read(
                                        RESOURCES_PATH + "configuration.yml"))
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addPersistentStorageData(
                                OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                                VALID_OAUTH2_TOKEN.apply(LocalDateTime.now().plusHours(1)))
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testSuccessfulPayment() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "payment_successful_case_mock_log.aap";
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithAuthCodeCallbackData(wireMockFilePath);

        // when
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testFailedPayment() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "payment_failed_case_mock_log.aap";
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithAuthCodeCallbackData(wireMockFilePath);

        // when
        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown).isExactlyInstanceOf(CreditorValidationException.class);
    }

    @Test
    public void testCanceledPayment() throws Exception {
        // given
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithErrorCallbackData();

        // when
        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationCancelledByUserException.class)
                .hasNoCause()
                .hasMessage("Authorisation of payment was cancelled. Please try again.");
    }

    private AgentWireMockPaymentTest createAgentWireMockPaymentTestWithAuthCodeCallbackData(
            String wireMockFilePath) throws Exception {
        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, wireMockFilePath)
                .withConfigurationFile(readAgentConfiguration())
                .addCallbackData("code", "DUMMY_AUTH_CODE")
                .withPayment(createDomesticPayment())
                .buildWithoutLogin(PaymentGBCommand.class);
    }

    private AgentWireMockPaymentTest createAgentWireMockPaymentTestWithErrorCallbackData()
            throws Exception {
        final String wireMockFilePath = RESOURCES_PATH + "payment_canceled_case_mock_log.aap";

        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, wireMockFilePath)
                .withConfigurationFile(readAgentConfiguration())
                .addCallbackData("error", "access_denied")
                .withPayment(createDomesticPayment())
                .buildWithoutLogin(PaymentGBCommand.class);
    }

    private AgentsServiceConfiguration readAgentConfiguration() throws Exception {
        return AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }

    private Payment createDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", "GBP");
        LocalDate executionDate = LocalDate.now();
        String currency = "GBP";
        String DESTINATION_IDENTIFIER = "04000469431111";
        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.SORT_CODE, DESTINATION_IDENTIFIER),
                                "Dummy name"))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "UK Demo"))
                .withUniqueId("4f0a6efca9c740e781419944143cdf13")
                .build();
    }
}
