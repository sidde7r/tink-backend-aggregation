package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIAuthenticatedTest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFITestConfig;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class HandelsbankenFIAutoAuthenticatorTest extends HandelsbankenFIAuthenticatedTest {

    @Test
    public void itWorks() throws Exception {
        autoAuthenticator.autoAuthenticate();

        assertThat(persistentStorage.getAuthorizeResponse(), not(Optional.empty()));
        assertThat(sessionStorage.applicationEntryPoint(), not(Optional.empty()));
    }

    @Override
    protected HandelsbankenFITestConfig getTestConfig() {
        return HandelsbankenFITestConfig.USER_2;
    }
}
