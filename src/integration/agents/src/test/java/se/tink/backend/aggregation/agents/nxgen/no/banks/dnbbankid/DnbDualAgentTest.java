package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;

public class DnbDualAgentTest {

    private final String USERNAME = "";
    private final String PHONE_NUMBER = "";

    @Test
    public void dualTest() throws Exception {

        DualAgentIntegrationTest.of(
                        new AgentIntegrationTest.Builder("no", "no-dnb")
                                .addCredentialField(Field.Key.USERNAME, USERNAME)
                                .addCredentialField(Field.Key.MOBILENUMBER, PHONE_NUMBER)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .expectLoggedIn(false)
                                .build(),
                        new AgentIntegrationTest.Builder("no", "no-dnb-ob")
                                .addCredentialField("PSU-ID", USERNAME)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .expectLoggedIn(false)
                                .build())
                .testAndCompare();
    }
}
