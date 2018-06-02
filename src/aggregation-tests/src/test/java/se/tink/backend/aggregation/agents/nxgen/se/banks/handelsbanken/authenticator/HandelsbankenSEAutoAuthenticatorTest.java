package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEAuthenticatedTest;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class HandelsbankenSEAutoAuthenticatorTest extends HandelsbankenSEAuthenticatedTest {

    @Test
    public void itWorks() throws Exception {
        autoAuthenticator.autoAuthenticate();

        assertThat(persistentStorage.getAuthorizeResponse(), not(Optional.empty()));
        assertThat(sessionStorage.applicationEntryPoint(), not(Optional.empty()));
    }
}
