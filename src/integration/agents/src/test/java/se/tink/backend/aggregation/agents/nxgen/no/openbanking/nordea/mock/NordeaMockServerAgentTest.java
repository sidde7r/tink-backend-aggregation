package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.mock;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static se.tink.libraries.enums.MarketCode.NO;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.mock.module.NordeaWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public class NordeaMockServerAgentTest {
    private static final String PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/nordea/mock/resources/";

    @Test
    public void shouldRetryThreeTimesAndFailWithBankError() throws Exception {
        // given
        final String wireMockFilePath = PATH + "nordea-bank-side-failure-refresh.aap";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(PATH + "configuration.yml");

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.builder(NO, "no-nordea-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withAgentModule(new NordeaWireMockTestModule())
                        .build();

        // when
        Throwable throwable = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // then
        assertThat(throwable)
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.BANK_SIDE_FAILURE");
    }
}
