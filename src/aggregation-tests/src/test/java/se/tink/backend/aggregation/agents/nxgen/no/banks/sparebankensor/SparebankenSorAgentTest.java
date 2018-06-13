package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.utils.CurrencyConstants;

public class SparebankenSorAgentTest extends NextGenerationBaseAgentTest<SparebankenSorAgent> {
    private static final String USERNAME = "username";
    private static final String MOBILENUMBER = "number";
    private static final String PASSWORD = "password";

    private Credentials credentials;

    public SparebankenSorAgentTest() {
        super(SparebankenSorAgent.class);
    }

    @Before
    public void setup() {
        credentials = new Credentials();
        credentials.setStatus(CredentialsStatus.CREATED);
        credentials.setUsername(USERNAME);
        credentials.setField(Field.Key.MOBILENUMBER, MOBILENUMBER);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testMultifactorLogin() throws Exception {
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
