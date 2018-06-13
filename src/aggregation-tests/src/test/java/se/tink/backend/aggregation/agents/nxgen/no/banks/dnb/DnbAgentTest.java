package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.utils.CurrencyConstants;

public class DnbAgentTest extends NextGenerationBaseAgentTest<DnbAgent> {
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
