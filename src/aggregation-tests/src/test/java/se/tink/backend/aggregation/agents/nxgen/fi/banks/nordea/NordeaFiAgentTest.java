package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.CurrencyConstants;

public class NordeaFiAgentTest extends NextGenerationAgentTest<NordeaFiAgent> {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private Credentials credentials;

    public NordeaFiAgentTest() {
        super(NordeaFiAgent.class);
    }

    @Before
    public void setup() {
        credentials = new Credentials();
        credentials.setUsername(USERNAME);
    }

    @Test
    public void testPasswordLogin() throws Exception {
        credentials.setPassword(PASSWORD);
        credentials.setType(CredentialsTypes.PASSWORD);

        testLogin(credentials);
    }

    @Test
    public void testRefresh() throws Exception {
        credentials.setPassword(PASSWORD);
        credentials.setType(CredentialsTypes.PASSWORD);

        testRefresh(credentials);
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.FI.getCode();
    }
}

