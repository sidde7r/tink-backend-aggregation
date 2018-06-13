package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.utils.CurrencyConstants;

@Ignore
public class SabadellAgentTest extends NextGenerationBaseAgentTest<SabadellAgent> {
    private final Credentials credentials = new Credentials();

    public SabadellAgentTest() {
        super(SabadellAgent.class);
    }

    @Before
    public void setup() {
        credentials.setField(Field.Key.USERNAME, "");
        credentials.setField(Field.Key.PASSWORD, "");
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
        return CurrencyConstants.ES.getCode();
    }
}
