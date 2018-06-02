package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularTestBase;

public class BancoPopularAuthenticatorTest extends BancoPopularTestBase {

    @Before
    public void setUp() throws Exception {
        super.setup();
    }

    @Test
    public void authenticateAllOk() throws Exception {
        authenticate();
    }

    @Test(expected = LoginException.class)
    public void authenticateBadPassword() throws Exception {
        password = "123456";
        authenticate();
    }
}
