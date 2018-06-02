package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaTestBase;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.TestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class NordeaAuthenticatorTest extends NordeaTestBase {

    @Before
    public void setUp() throws Exception {
        setUpTest();
    }

    @Test
    public void authenticate() throws Exception {
        authenticator.authenticate(TestConfig.USERNAME, TestConfig.PASSWORD);

        verify(tinkHttpClient).addPersistentHeader(eq(NordeaV20Constants.HeaderKey.SECURITY_TOKEN), anyString());
    }
}
