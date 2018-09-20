package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class BanquePopulaireAgentTest {
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    @Test
    public void testRegisterAndRefresh() throws Exception {
        AgentIntegrationTest.Builder builder = new AgentIntegrationTest.Builder("fr",
                "fr-banquepopulaire-valdefrance-password")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true);

        builder.build()
                .testRefresh();
    }
}
