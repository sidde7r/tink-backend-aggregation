package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.AUTH_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.PROVIDER_NAME;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.STATE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.createDomesticPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander.integration.SantanderAgentWiremockTestFixtures.createFarFutureDomesticPayment;

import java.time.LocalDateTime;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentGBCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.PartyDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Payment;

public class SantanderAgentWiremockTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/santander/integration/resources/";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";

    public static final String AIS_ACCESS_TOKEN_KEY = "open_id_ais_access_token";
    private static final String EXPIRED_OAUTH2_TOKEN =
            "{\"expires_in\" : 0, \"issuedAt\": 1598516000, \"token_type\":\"bearer\","
                    + " \"access_token\":\"EXPIRED_DUMMY_ACCESS_TOKEN\", \"refreshToken\":\"DUMMY_REFRESH_TOKEN\"}";

    @Test
    public void testPaymentSuccessfulPayment() throws Exception {
        // given
        final String wireMockFilePath =
                RESOURCES_PATH + "santander_payment_successful_case_mock_log.aap";
        final Payment payment = createDomesticPayment();
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithAuthCodeCallbackData(wireMockFilePath, payment);

        // when
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testPaymentFailedCase() throws Exception {
        // given
        final String wireMockFilePath =
                RESOURCES_PATH + "santander_payment_failed_case_mock_log.aap";
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
                .isExactlyInstanceOf(PaymentAuthorizationCancelledByUserException.class)
                .hasNoCause()
                .hasMessage("Authorisation of payment was cancelled. Please try again.");
    }

    private static AgentWireMockPaymentTest createAgentWireMockPaymentTestWithAuthCodeCallbackData(
            String wireMockFilePath, Payment payment) throws Exception {
        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, wireMockFilePath)
                .withConfigurationFile(readAgentConfiguration())
                .addCallbackData("code", AUTH_CODE)
                .withPayment(payment)
                .buildWithoutLogin(PaymentGBCommand.class);
    }

    private static AgentWireMockPaymentTest createAgentWireMockPaymentTestWithErrorCallbackData(
            Payment payment) throws Exception {
        final String wireMockFilePath =
                RESOURCES_PATH + "santander_payment_canceled_case_mock_log.aap";

        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, wireMockFilePath)
                .withConfigurationFile(readAgentConfiguration())
                .addCallbackData("state", STATE)
                .addCallbackData("error", "access_denied")
                .withPayment(payment)
                .buildWithoutLogin(PaymentGBCommand.class);
    }

    @Test
    public void shouldRunFullAuthRefreshSuccessfully() throws Exception {

        // given
        final String wireMockFilePath = RESOURCES_PATH + "full-auth-refresh.aap";
        final String contractFilePath = RESOURCES_PATH + "full-auth-refresh.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.SAVING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.TRANSFER_DESTINATIONS)
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .enableDataDumpForContractFile()
                        .enableHttpDebugTrace()
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
    public void shouldRestorePartyDataSuccessfully() throws Exception {

        // given
        final String wireMockFilePath = RESOURCES_PATH + "party-data-not-fetched.aap";
        final String contractFilePath = RESOURCES_PATH + "party-data-restored.json";

        final String party =
                "{\"PartyId\": \"00000000000000000000\", \"FullLegalName\":\"MR JOHN TINK\"}";
        final String parties =
                "[{\"PartyId\": \"11111111111111111111\", \"FullLegalName\":\"MR ADAM TINK\"},{\"PartyId\": \"22222222222222222222\", \"FullLegalName\":\"MRS ANNA TINK\"}]";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(AIS_ACCESS_TOKEN_KEY, EXPIRED_OAUTH2_TOKEN)
                        .addPersistentStorageData(
                                ScaExpirationValidator.LAST_SCA_TIME,
                                LocalDateTime.now().minusMinutes(6).toString())
                        .addPersistentStorageData(PartyDataStorage.RECENT_PARTY_DATA, party)
                        .addPersistentStorageData(PartyDataStorage.RECENT_PARTY_DATA_LIST, parties)
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

    /** Test for agents currently with SCA expired but without party data stored */
    @Test
    public void shouldNotFetchPartyData() throws Exception {

        // given
        final String wireMockFilePath = RESOURCES_PATH + "party-data-not-fetched.aap";
        final String contractFilePath = RESOURCES_PATH + "party-data-not-fetched.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addPersistentStorageData(AIS_ACCESS_TOKEN_KEY, EXPIRED_OAUTH2_TOKEN)
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

    private static AgentsServiceConfiguration readAgentConfiguration() throws Exception {
        return AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }
}
