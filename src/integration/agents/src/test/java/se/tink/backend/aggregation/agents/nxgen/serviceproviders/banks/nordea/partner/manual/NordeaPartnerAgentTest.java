package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;

public class NordeaPartnerAgentTest {
    enum ArgumentEnum implements ArgumentManagerEnum {
        MARKET,
        USERNAME;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<ArgumentEnum> manager =
            new ArgumentManager<>(ArgumentEnum.values());

    @Before
    public void setup() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        final String market = manager.get(ArgumentEnum.MARKET);
        new AgentIntegrationTest.Builder(market, market + "-nordeapartner-jwt")
                .addCredentialField(Field.Key.USERNAME, manager.get(ArgumentEnum.USERNAME))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .setClusterId("neston-staging")
                .build()
                .testRefresh();
    }
}
