package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class TargobankAgentTest {
    private final ArgumentManager<ArgumentManager.UsernamePasswordArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.UsernamePasswordArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("de", "de-targobank-ob")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                manager.get(ArgumentManager.UsernamePasswordArgumentEnum.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                manager.get(ArgumentManager.UsernamePasswordArgumentEnum.PASSWORD))
                        .loadCredentialsBefore(false)
                        .expectLoggedIn(false)
                        .saveCredentialsAfter(false)
                        .setAppId("tink")
                        .dumpContentForContractFile()
                        .setFinancialInstitutionId("targobank");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    @AfterClass
    public static void cleanup() {
        ArgumentManager.afterClass();
    }
}
