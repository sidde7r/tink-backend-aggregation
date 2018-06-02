package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgentTest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.CurrencyConstants;

public class SparebankenVestAgentTest extends NextGenerationAgentTest<SparebankenVestAgent> {
    private static final String USERNAME = "username";
    private static final String ACTIVATION_CODE = "activationCode";

    private Credentials credentials;

    public SparebankenVestAgentTest() {
        super(SparebankenVestAgent.class);
    }

    @Before
    public void setup() {
        credentials = new Credentials();
        credentials.setStatus(CredentialsStatus.CREATED);
        credentials.setUsername(USERNAME);
    }

    @Test
    public void testPasswordLogin() throws Exception {
        credentials.setPassword(ACTIVATION_CODE);
        credentials.setType(CredentialsTypes.PASSWORD);

        testLogin(credentials);
    }

    @Test
    public void testRefresh() throws Exception {
        credentials.setPassword(ACTIVATION_CODE);
        credentials.setType(CredentialsTypes.PASSWORD);

        testRefresh(credentials);
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.NO.getCode();
    }
}
