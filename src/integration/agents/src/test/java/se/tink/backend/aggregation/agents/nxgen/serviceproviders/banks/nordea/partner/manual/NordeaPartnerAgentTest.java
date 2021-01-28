package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;

public class NordeaPartnerAgentTest {
    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

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
        new AgentIntegrationTest.Builder("se", "se-nordeapartner-jwt")
                .addCredentialField(Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .setClusterId("neston-staging")
                .build()
                .testRefresh();
    }
}
