package se.tink.backend.rpc.application;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.core.User;
import se.tink.libraries.application.ApplicationType;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateApplicationCommandTest {
    private final static String userAgent = "Tink/4.0.0 (iOS; 8.1, iPhone Simulator)";

    @Test
    public void testPropertyMapping() {
        final User user = new User();
        final ApplicationType applicationType = ApplicationType.RESIDENCE_VALUATION;

        CreateApplicationCommand command = new CreateApplicationCommand(user, Optional.of(userAgent), applicationType);

        assertThat(command.getUser()).isEqualTo(user);
        assertThat(command.getTinkUserAgent()).isNotNull();
        assertThat(command.getApplicationType()).isEqualTo(applicationType);
    }

    @Test(expected = NullPointerException.class)
    public void testNoUser() {
        new CreateApplicationCommand(null, Optional.of(userAgent), ApplicationType.RESIDENCE_VALUATION);
    }

    @Test(expected = NullPointerException.class)
    public void testNoApplicationType() {
        new CreateApplicationCommand(new User(), Optional.of(userAgent), null);
    }
}
