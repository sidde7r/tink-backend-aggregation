package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgentTest;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.utils.CurrencyConstants;

public class SparebankenSorAgentTest extends NextGenerationAgentTest<SparebankenSorAgent> {
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
