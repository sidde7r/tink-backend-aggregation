package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class IccreaAgentTest {

    private final ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordManager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        usernamePasswordManager.before();

        builder =
                new AgentIntegrationTest.Builder("it", "it-iccrea-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("iccrea")
                        .setAppId("tink")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                usernamePasswordManager.get(UsernamePasswordArgumentEnum.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                usernamePasswordManager.get(UsernamePasswordArgumentEnum.PASSWORD));
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
