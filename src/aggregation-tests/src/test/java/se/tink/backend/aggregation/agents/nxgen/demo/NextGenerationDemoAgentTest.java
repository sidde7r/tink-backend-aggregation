package se.tink.backend.aggregation.agents.nxgen.demo;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.utils.CurrencyConstants;

public class NextGenerationDemoAgentTest extends NextGenerationBaseAgentTest<NextGenerationDemoAgent> {
    private final Credentials credentials = new Credentials();

    @Before
    public void setup() {
        credentials.setField(Field.Key.USERNAME, "201212121212");
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
    }

    public NextGenerationDemoAgentTest() {
        super(NextGenerationDemoAgent.class);
    }

    @Test
    public void testBankIdLogin() throws Exception {
        testLogin(credentials);
    }

    @Test
    public void testRefresh() throws Exception {
        testRefresh(credentials);
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.SE.getCode();
    }
}
