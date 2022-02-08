package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import org.junit.AfterClass;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.DkSSMethod;

public class NordeaDkAgentTest {

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testLoginAndRefreshNemId() throws Exception {
        new AgentIntegrationTest.Builder("dk", "dk-nordea-nemid")
                .addCredentialField(
                        Field.Key.AUTH_METHOD_SELECTOR, DkSSMethod.NEM_ID.getSupplementalInfoKey())
                .expectLoggedIn(false)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }

    @Test
    public void testLoginAndRefreshMitId() throws Exception {
        new AgentIntegrationTest.Builder("dk", "dk-nordea-nemid")
                .addCredentialField(
                        Field.Key.AUTH_METHOD_SELECTOR, DkSSMethod.MIT_ID.getSupplementalInfoKey())
                .expectLoggedIn(false)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .build()
                .testRefresh();
    }
}
