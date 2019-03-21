package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public final class DanskeBankSEAgentTest {
    private enum Arg {
        SSN,
        SERVICE_CODE,
    }

    private static final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefreshWithBankId() throws Exception {
        builder =
                new AgentIntegrationTest.Builder("se", "se-danskebank-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
        builder.build().testRefresh();
    }

    @Test
    public void testRefreshWithServiceCode() throws Exception {
        builder =
                new AgentIntegrationTest.Builder("se", "se-danskebank-password")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
                        .addCredentialField(Field.Key.PASSWORD, manager.get(Arg.SERVICE_CODE))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
        builder.build().testRefresh();
    }
}
