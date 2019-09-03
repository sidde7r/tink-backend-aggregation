package se.tink.backend.aggregation.agents.nxgen.dk.banks.bankdatadk;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.DualAgentIntegrationTest;

@Ignore
public class BankdataDkDualAgentIntegrationTest {

    // For RE agent
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    // For OB agent
    private static final String IBAN = "";

    @Test
    public void dualTest() throws Exception {

        DualAgentIntegrationTest.of(
                        new AgentIntegrationTest.Builder("dk", "dk-ringkjobinglandbobank-password")
                                .addCredentialField(Field.Key.USERNAME, USERNAME)
                                .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                                .expectLoggedIn(false)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .build(),
                        new AgentIntegrationTest.Builder("dk", "dk-ringkjobinglandbobank-ob")
                                .addCredentialField("iban", IBAN)
                                .loadCredentialsBefore(false)
                                .saveCredentialsAfter(false)
                                .expectLoggedIn(false)
                                .build())
                .testAndCompare();
    }
}
