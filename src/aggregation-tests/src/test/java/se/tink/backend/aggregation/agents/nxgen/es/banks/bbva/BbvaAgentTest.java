package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class BbvaAgentTest extends NextGenerationAgentTest<BbvaAgent> {
    private final Credentials credentials = new Credentials();

    public BbvaAgentTest() {
        super(BbvaAgent.class);
    }

    @Before
    public void setup() {
        credentials.setField(Field.Key.USERNAME, "ccccccccc");
        credentials.setField(Field.Key.PASSWORD, "nnnnnn");
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

    @Override
    public String getCurrency() {
        return "EUR";
    }
}
