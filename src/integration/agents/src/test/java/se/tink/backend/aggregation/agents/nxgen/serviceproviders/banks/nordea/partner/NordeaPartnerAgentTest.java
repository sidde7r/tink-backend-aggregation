package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;

public class NordeaPartnerAgentTest {
    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());

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
                .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .setClusterId("neston-staging")
                .build()
                .testRefresh();
    }
}
