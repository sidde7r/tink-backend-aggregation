package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.mock;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.enums.MarketCode;

public class DnbAgentWiremockTest {
    private static final String RESOURCES_BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/mock/resources/";

    @Test
    public void testAuthenticateManual() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(getResourcePath("configuration.yml"));

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NO)
                        .withProviderName("no-dnb-ob")
                        .withWireMockFilePath(getResourcePath("dnb_auth_manual.aap"))
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCredentialField(DnbConstants.HeaderKeys.PSU_ID, "dummyPsuId")
                        .build();

        // then
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void testRefreshAuto() throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(getResourcePath("configuration.yml"));

        UserAvailability ua = new UserAvailability();
        ua.setUserPresent(false);
        ua.setUserAvailableForInteraction(false);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NO)
                        .withProviderName("no-dnb-ob")
                        .withWireMockFilePath(getResourcePath("dnb_refresh_auto.aap"))
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addPersistentStorageData(DnbConstants.IdTags.CONSENT_ID, "dummyConsentId")
                        .withUserAvailability(ua)
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(
                                getResourcePath("dnb_refresh_auto_contract.json"));

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testRefreshManualWithoutPagination() throws Exception {
        testRefreshManual(
                "dnb_refresh_manual_without_pagination.aap",
                "dnb_refresh_manual_without_pagination_contract.json");
    }

    @Test
    public void testRefreshManualWithPagination() throws Exception {
        testRefreshManual(
                "dnb_refresh_manual_with_pagintion.aap",
                "dnb_refresh_manual_with_pagination_contract.json");
    }

    private void testRefreshManual(String aapFileName, String contractFileName) throws Exception {
        // given
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(getResourcePath("configuration.yml"));

        UserAvailability ua = new UserAvailability();
        ua.setUserPresent(true);
        ua.setUserAvailableForInteraction(true);
        ua.setOriginatingUserIp("0.0.0.0");

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NO)
                        .withProviderName("no-dnb-ob")
                        .withWireMockFilePath(getResourcePath(aapFileName))
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addPersistentStorageData(DnbConstants.IdTags.CONSENT_ID, "dummyConsentId")
                        .addCredentialField(DnbConstants.HeaderKeys.PSU_ID, "dummyPsuId")
                        .withUserAvailability(ua)
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(getResourcePath(contractFileName));

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    private String getResourcePath(String fileName) {
        return RESOURCES_BASE_PATH + fileName;
    }
}
