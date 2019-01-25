package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.CurrencyConstants;

public class NordeaNoAgentTest extends NextGenerationBaseAgentTest<NordeaNoAgent> {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private Credentials credentials;

    public NordeaNoAgentTest() {
        super(NordeaNoAgent.class);
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
    public void testBankIdLogin() throws Exception {
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

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
        return CurrencyConstants.NO.getCode();
    }
}
