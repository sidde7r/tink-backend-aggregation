package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.CurrencyConstants;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgentTest;

public class DnbAgentTest extends NextGenerationAgentTest<DnbAgent> {
    private final Credentials credentials = new Credentials();

    public DnbAgentTest() {
        super(DnbAgent.class);
    }

    @Before
    public void setup() {
        credentials.setField(Field.Key.USERNAME, "ddmmyynnnnn");
        credentials.setField(Field.Key.MOBILENUMBER, "nnnnnnnn");
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
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
