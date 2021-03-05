package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.monzo.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.monzo.mock.MonzoAgentWiremockTestFixtures.AUTH_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.monzo.mock.MonzoAgentWiremockTestFixtures.PROVIDER_NAME;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.monzo.mock.MonzoAgentWiremockTestFixtures.STATE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.monzo.mock.MonzoAgentWiremockTestFixtures.createDomesticPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.monzo.mock.MonzoAgentWiremockTestFixtures.createFarFutureDomesticPayment;

import java.time.LocalDateTime;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentGBCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Payment;

public class MonzoAgentWiremockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/monzo/mock/resources/";
    static final String configFilePath = RESOURCES_PATH + "configuration.yml";

    private static final String EXPIRED_OAUTH2_TOKEN =
            "{\"expires_in\" : 0, \"issuedAt\": 1598516000, \"token_type\":\"bearer\",  \"access_token\":\"EXPIRED_DUMMY_ACCESS_TOKEN\", \"refreshToken\":\"DUMMY_REFRESH_TOKEN\"}";

    @Test
    public void testPaymentSuccessfulPayment() throws Exception {
        // given
        final String wireMockFilePath =
                RESOURCES_PATH + "monzo_payment_successful_case_mock_log.aap";
        final Payment payment = createDomesticPayment();
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithAuthCodeCallbackData(wireMockFilePath, payment);

        // when
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testPaymentFailedCase() throws Exception {
        // given
        final String wireMockFilePath = RESOURCES_PATH + "monzo_payment_failed_case_mock_log.aap";
        final Payment payment = createFarFutureDomesticPayment();
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithAuthCodeCallbackData(wireMockFilePath, payment);

        // when
        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(HttpResponseException.class)
                .hasNoCause()
                .hasMessage(
                        "Response statusCode: 400 with body: {\"Code\":\"400\",\"Message\":\"There is something wrong with the request parameters provided\",\"Errors\":[{\"ErrorCode\":\"UK.OBIE.Field.InvalidDate\",\"Message\":\"The date field data.initiation.requestedExecutionDateTime is invalid\"}]}");
    }

    @Test
    public void testPaymentCancelledCase() throws Exception {
        // given
        final Payment payment = createDomesticPayment();
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithErrorCallbackData(payment);

        // when
        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationException.class)
                .hasNoCause()
                .hasMessage("Payment was not authorised. Please try again.");
    }

    private static AgentWireMockPaymentTest createAgentWireMockPaymentTestWithAuthCodeCallbackData(
            String wireMockFilePath, Payment payment) throws Exception {
        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, wireMockFilePath)
                .withConfigurationFile(AgentsServiceConfigurationReader.read(configFilePath))
                .addCallbackData("code", AUTH_CODE)
                .withPayment(payment)
                .buildWithoutLogin(PaymentGBCommand.class);
    }

    private static AgentWireMockPaymentTest createAgentWireMockPaymentTestWithErrorCallbackData(
            Payment payment) throws Exception {
        final String wireMockFilePath = RESOURCES_PATH + "monzo_payment_canceled_case_mock_log.aap";

        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, wireMockFilePath)
                .withConfigurationFile(AgentsServiceConfigurationReader.read(configFilePath))
                .addCallbackData("state", STATE)
                .addCallbackData("error", "access_denied")
                .withPayment(payment)
                .buildWithoutLogin(PaymentGBCommand.class);
    }

    @Test
    public void manualRefreshAll() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "manual-refresh-all.aap";
        final String wireMockContractFilePath = RESOURCES_PATH + "manual-refresh-all-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(AgentsServiceConfigurationReader.read(configFilePath))
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "DUMMY_ACCESS_TOKEN2")
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    /**
     * * https://docs.monzo.com/#parties Endpoint /party expires 5 min after last SCA In case of
     * auto authentication data will be retrieved from persistent storage
     */
    @Test
    public void restoreIdentityData() throws Exception {
        // Given
        final String wireMockServerFilePath = RESOURCES_PATH + "restore-identity-data.aap";
        final String wireMockContractFilePath =
                RESOURCES_PATH + "restore-identity-data-contract.json";

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final String identityDataV31Entity =
                "{\"PartyId\": \"user_11119x3OpXVihQdEO1EoXC\", \"Name\": \"John Tinker\", \"FullLegalName\":\"John The Tinker2\"}";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(AgentsServiceConfigurationReader.read(configFilePath))
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(
                                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                                EXPIRED_OAUTH2_TOKEN)
                        .addPersistentStorageData(
                                UkOpenBankingV31Constants.PersistentStorageKeys.LAST_SCA_TIME,
                                LocalDateTime.now().minusMinutes(6).toString())
                        .addPersistentStorageData(
                                MonzoConstants.RECENT_IDENTITY_DATA, identityDataV31Entity)
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
                        .build();

        // When
        agentWireMockRefreshTest.executeRefresh();

        // Then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
