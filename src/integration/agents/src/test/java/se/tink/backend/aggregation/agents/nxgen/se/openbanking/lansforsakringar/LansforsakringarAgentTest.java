package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;

public class LansforsakringarAgentTest {

    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("SE", "se-lansforsakringar-ob")
                        .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                        .setFinancialInstitutionId("lansforsakringar")
                        .setAppId("tink")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
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
