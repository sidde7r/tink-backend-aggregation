package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.libraries.enums.MarketCode.DE;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public class Xs2aMockServerAgentTest {
    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/xs2adevelopers/mock/resources/";
    private static final String CONFIGURATION_PATH = BASE_PATH + "configuration.yml";

    @Test
    public void test_refresh_with_pagination_error() throws Exception {

        // given
        final String wireMockFilePath = BASE_PATH + "commerz-paging-with-error-refresh.aap";

        final String contractFilePath = BASE_PATH + "agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(DE, "de-commerzbank-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "woot")
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
    public void test_refresh_with_a_lot_of_pages() throws Exception {

        // given
        final String wireMockFilePath = BASE_PATH + "comdirect-many-pages-refresh.aap";

        final String contractFilePath = BASE_PATH + "comdirect-many-pages-result.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(DE, "de-comdirect-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "woot")
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
    public void should_end_with_proper_exception_when_consent_creation_fails() throws Exception {
        final String wireMockFilePath = BASE_PATH + "consent-creation-failed.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-comdirect-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .build();

        // when
        Throwable throwable = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // then
        assertThat(throwable)
                .isInstanceOf(BankServiceException.class)
                .hasMessage(
                        "Failed to create consent due to error BERLINGROUP_AUTHORIZATION_SCA_CREATION_FAILED");
    }
}
