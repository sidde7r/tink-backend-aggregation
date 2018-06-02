package se.tink.backend.aggregation.agents.nxgen.de.banks.n26;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;

public class N26AgentTest extends NextGenerationAgentTest<N26Agent> {

    public static final String USERNAME = "";
    public static final String PASSWORD = "";

    private Credentials credentials;

    public N26AgentTest(){
        super(N26Agent.class);
    }

    @Override
    public String getCurrency() {
        return N26Constants.CURRENCY_EUR;
    }

    @Before
    public void setup(){
        credentials = new Credentials();
        credentials.setStatus(CredentialsStatus.CREATED);
        credentials.setUsername(USERNAME);
    }

    @Test
    public void testPasswordLogin() throws Exception{
        credentials.setPassword(PASSWORD);
        testLogin(credentials);
    }

    @Test
    public void testRefresh() throws Exception {
        credentials.setPassword(PASSWORD);
        credentials.setType(CredentialsTypes.PASSWORD);

        testRefresh(credentials);
    }
}
