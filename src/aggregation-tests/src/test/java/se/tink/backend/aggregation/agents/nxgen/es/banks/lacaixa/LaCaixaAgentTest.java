package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.utils.CurrencyConstants;

@Ignore
public class LaCaixaAgentTest extends NextGenerationBaseAgentTest<LaCaixaAgent> {

    private final Credentials credentials = new Credentials();

    public LaCaixaAgentTest() {
        super(LaCaixaAgent.class);
    }

    @Before
    public void setup(){
        credentials.setField(Field.Key.USERNAME, "CCCCCCCC");
        credentials.setField(Field.Key.PASSWORD, "NNNNNN");
        credentials.setType(CredentialsTypes.PASSWORD);
    }

    @Test
    public void testPasswordLogin() throws Exception{
        testLogin(credentials);
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.ES.getCode();
    }
}
