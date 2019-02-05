package se.tink.backend.aggregation.agents.nxgen.fr.banks.cic;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.targo.TargoBankDEAgent;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.CurrencyConstants;

@Ignore
public class CicBankAgentTest extends NextGenerationBaseAgentTest<CicBankAgent> {
    private static final String USERNAME = "<username>";
    private static final String PASSWORD = "<password>";

    private Credentials credentials;

    public CicBankAgentTest() {
        super(CicBankAgent.class);
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
