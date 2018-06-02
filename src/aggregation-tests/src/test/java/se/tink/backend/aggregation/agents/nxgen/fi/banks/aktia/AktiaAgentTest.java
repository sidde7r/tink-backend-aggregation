package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.CurrencyConstants;

public class AktiaAgentTest extends NextGenerationAgentTest<AktiaAgent> {
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
