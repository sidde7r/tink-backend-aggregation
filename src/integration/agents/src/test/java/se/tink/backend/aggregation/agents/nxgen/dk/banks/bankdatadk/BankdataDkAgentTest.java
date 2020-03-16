package se.tink.backend.aggregation.agents.nxgen.dk.banks.bankdatadk;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdAuthenticationController.NEMID_PASSWORD_FIELD_NAME;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class BankdataDkAgentTest {
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final String ACCESS_PIN = "";

    private AgentIntegrationTest.Builder builder() {
        return new AgentIntegrationTest.Builder("dk", "dk-ringkjobinglandbobank-password")
                .addCredentialField(Field.Key.USERNAME, USERNAME)
                .addCredentialField(NEMID_PASSWORD_FIELD_NAME, PASSWORD)
                .addCredentialField(Field.Key.ACCESS_PIN, ACCESS_PIN)
                .expectLoggedIn(false)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true);
    }

    @Test
    public void testRefresh() throws Exception {
        builder().build().testRefresh();
    }
}
