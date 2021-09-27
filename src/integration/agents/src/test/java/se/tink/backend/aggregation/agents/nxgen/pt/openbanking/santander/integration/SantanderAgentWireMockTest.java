package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.santander.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.module.SibsWireMockTestModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.SibsAccountSegment;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.Consent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.enums.MarketCode;

public class SantanderAgentWireMockTest {

    private AgentsServiceConfiguration configuration;
    private AgentContractEntity expectedData;
    private Consent consent;

    private static final String PROVIDER_NAME = "pt-santander-oauth2";
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/openbanking/santander/integration/resources/";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "configuration.yml";
    private static final String AGENT_CONTRACT_PATH = RESOURCES_PATH + "agent_contract.json";

    @Before
    public void init() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        expectedData =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(AGENT_CONTRACT_PATH);
        consent =
                new Consent("DUMMY_CONSENT_ID", new ConstantLocalDateTimeSource().now().toString());
        //            LocalDateTime.of(2020, 01, 01, 00, 00).toString());
    }

    @Test
    public void shouldFullAuthenticateAndRefresh() throws Exception {

        // given
        final String successfulManualAuthenticationWireMockFilePath =
                RESOURCES_PATH + "successful_full_authentication.aap";

        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(true);
        userAvailability.setUserAvailableForInteraction(true);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.PT)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(successfulManualAuthenticationWireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .withUserAvailability(userAvailability)
                        .addCallbackData("accountSegment", "PERSONAL")
                        .withAgentTestModule(new SibsWireMockTestModule())
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expectedData);
    }

    @Test
    public void shouldAutoRefresh() throws Exception {

        // given
        final String successfulAutoRefreshWireMockFilePath =
                RESOURCES_PATH + "successful_auto_refresh.aap";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildAgentWireMockAutoRefreshTest(successfulAutoRefreshWireMockFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expectedData);
    }

    @Test
    public void shouldThrowOnExceedingMultiplicityPerDayErrorWithAutoRefresh() {

        // given
        final String errorAutoRefreshWireMockFilePath =
                RESOURCES_PATH + "exceeding_multiplicity_auto_refresh.aap";

        final String exceptionMessage =
                "Http status: 429 Error body: "
                        + "{\"transactionStatus\": \"RJCT\",\"tppMessages\": "
                        + "[{\"category\": \"ERROR\",\"code\": \"ACCESS_EXCEEDED\",\"text\": "
                        + "\"The access on the account has been exceeding the consented multiplicity per day.\"}]}";

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                buildAgentWireMockAutoRefreshTest(errorAutoRefreshWireMockFilePath);

        // when
        final Throwable throwable = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage(exceptionMessage);
    }

    private AgentWireMockRefreshTest buildAgentWireMockAutoRefreshTest(String wireMockFilePath) {

        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(false);

        return AgentWireMockRefreshTest.nxBuilder()
                .withMarketCode(MarketCode.PT)
                .withProviderName(PROVIDER_NAME)
                .withWireMockFilePath(wireMockFilePath)
                .withConfigFile(configuration)
                .testAutoAuthentication()
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .withUserAvailability(userAvailability)
                .addPersistentStorageData("ACCOUNT_SEGMENT", SibsAccountSegment.PERSONAL)
                .addPersistentStorageData("CONSENT_ID", consent)
                .withAgentTestModule(new SibsWireMockTestModule())
                .build();
    }
}
