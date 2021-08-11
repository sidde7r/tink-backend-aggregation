package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;

public class NordeaSeBusinessAgentTest {

    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<UsernameArgumentEnum> psuManager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @Before
    public void setup() {
        ssnManager.before();
        psuManager.before();
        builder =
                new AgentIntegrationTest.Builder("SE", "se-nordea-business-ob")
                        .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                        .addCredentialField(
                                Key.CORPORATE_ID, psuManager.get(UsernameArgumentEnum.USERNAME))
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("nordea")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
