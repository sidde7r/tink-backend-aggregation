package se.tink.backend.aggregation.nxgen.agents.se.serviceproviders.creditcards.amex;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62.AmericanExpressV62SEAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.utils.CurrencyConstants;

@Ignore
public class AmericanExpressV62AgentTest extends NextGenerationAgentTest<AmericanExpressV62SEAgent> {
    private final Credentials credentials = new Credentials();

    public AmericanExpressV62AgentTest() {
        super(AmericanExpressV62SEAgent.class);
    }

    @Before
    public void setup() {
        credentials.setField(Field.Key.USERNAME, "<username>");
        credentials.setField(Field.Key.PASSWORD, "<password>");
        credentials.setType(CredentialsTypes.PASSWORD);
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
