package se.tink.backend.aggregation.agents.nxgen.demo.agenttest;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.currency.CurrencyConstants;
import se.tink.backend.aggregation.nxgen.agents.agenttest.NextGenerationAgentTest;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;

public class NextGenerationDemoAgentTest extends NextGenerationAgentTest<NextGenerationDemoAgent> {
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
