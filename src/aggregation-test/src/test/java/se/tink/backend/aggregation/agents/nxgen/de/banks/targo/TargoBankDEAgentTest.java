package se.tink.backend.aggregation.agents.nxgen.de.banks.targo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.targo.TargoBankDEAgent;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.CurrencyConstants;

@Ignore
public class TargoBankDEAgentTest extends NextGenerationBaseAgentTest<TargoBankDEAgent> {
    private static final String USERNAME = "<username>";
    private static final String PASSWORD = "<password>";
    private Credentials credentials;

    public TargoBankDEAgentTest() {
        super(TargoBankDEAgent.class);
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.DE.getCode();
    }

    @Before
    public void setup() {
        credentials = new Credentials();
        credentials.setUsername(USERNAME);
        credentials.setPassword(PASSWORD);
        credentials.setType(CredentialsTypes.PASSWORD);
    }

    @Test
    public void testPasswordLogin() throws Exception {
        testLogin(credentials);
    }

    @Test
    public void testRefresh() throws Exception {
        testRefresh(credentials);
    }
}
