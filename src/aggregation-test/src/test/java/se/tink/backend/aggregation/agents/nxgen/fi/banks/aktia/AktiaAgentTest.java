package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.CurrencyConstants;

public class AktiaAgentTest extends NextGenerationBaseAgentTest<AktiaAgent> {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private Credentials credentials;

    public AktiaAgentTest() {
        super(AktiaAgent.class);
    }

    @Before
    public void setup() {
        credentials = new Credentials();
        credentials.setStatus(CredentialsStatus.CREATED);
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
