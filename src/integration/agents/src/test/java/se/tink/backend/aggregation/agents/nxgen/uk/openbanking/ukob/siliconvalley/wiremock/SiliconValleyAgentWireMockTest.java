package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.siliconvalley.wiremock;

import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class SiliconValleyAgentWireMockTest {

    private static final String PROVIDER_NAME = "uk-siliconvalley-business-ob";

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/ukob/siliconvalley/wiremock/resources/";

    private static final String FULL_AUTH_FETCH_DATA_TRAFFIC =
            RESOURCES_PATH + "full-auth-fetch-data.aap";
    private static final String FULL_AUTH_FETCH_DATA_CONTRACT =
            RESOURCES_PATH + "full-auth-fetch-data.json";

    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "config.yml";

    private static final String CODE_PARAM = "code";
    private static final String DUMMY_CODE = "DUMMY_CODE";

    @Test
    public void shouldRunFullAuthWithDataRefreshSuccessfully() throws Exception {
        // given
        Set<RefreshableItem> itemsExpectedToBeRefreshed =
                Sets.newHashSet(
                        RefreshableItem.CHECKING_ACCOUNTS, RefreshableItem.CHECKING_TRANSACTIONS);

        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(FULL_AUTH_FETCH_DATA_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testFullAuthentication()
                        .withRefreshableItems(itemsExpectedToBeRefreshed)
                        .addCallbackData(CODE_PARAM, DUMMY_CODE)
                        .enableHttpDebugTrace()
                        .build();

        // when
        test.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(FULL_AUTH_FETCH_DATA_CONTRACT);
        test.assertExpectedData(expected);
    }
}
