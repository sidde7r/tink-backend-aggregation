package se.tink.backend.rpc.application;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.core.User;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationListCommandTest {
    private final static String userAgent = "Tink/4.0.0 (iOS; 8.1, iPhone Simulator)";

    @Test
    public void testPropertyMapping() {
        final User user = new User();

        ApplicationListCommand command = new ApplicationListCommand(user, Optional.of(userAgent));

        assertThat(command.getUser()).isEqualTo(user);
        assertThat(command.getUserId()).isEqualTo(UUIDUtils.fromTinkUUID(user.getId()));
        assertThat(command.getTinkUserAgent()).isNotNull();
    }

    @Test(expected = NullPointerException.class)
    public void testNoUser() {
        new ApplicationListCommand(null, Optional.of(userAgent));
    }
}
