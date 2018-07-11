package se.tink.backend.aggregation.agents.nxgen.fr.banks.cm;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.CurrencyConstants;

public class CreditMutuelAgentTest extends NextGenerationBaseAgentTest<CreditMutuelAgent> {

    private static final String USERNAME = "<username>";
    private static final String PASSWORD = "<password>";

    private Credentials credentials;

    public CreditMutuelAgentTest() {
        super(CreditMutuelAgent.class);
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.FR.getCode();
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
