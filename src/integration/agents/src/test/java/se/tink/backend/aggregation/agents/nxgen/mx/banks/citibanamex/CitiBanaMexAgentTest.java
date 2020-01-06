package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;

public class CitiBanaMexAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> helper =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("mx", "mx-citibana-password")
                .addCredentialField(
                        Field.Key.USERNAME, helper.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(
                        Field.Key.PASSWORD, helper.get(UsernamePasswordArgumentEnum.PASSWORD))
                .setFinancialInstitutionId("CitiBanaMex")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .doLogout(true)
                .build()
                .testRefresh();
    }
}
